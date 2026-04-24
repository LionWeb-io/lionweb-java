package io.lionweb.serialization.data;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Lower-level representation of a Classifier Instance (either a Node or an AnnotationInstance) used
 * during serialization and deserialization. Note that "broken" classifier instances (e.g. with null
 * IDs or missing MetaPointers) can also be represented.
 *
 * <h3>Two internal representations</h3>
 *
 * <p>This class maintains two mutually-exclusive internal representations:
 *
 * <ol>
 *   <li><b>Schema mode</b> (active when {@link #schema} is non-null): feature values are held in
 *       parallel arrays ({@code propertyValues[]}, {@code containmentValues[]}, {@code
 *       referenceValues[]}) indexed positionally against the {@link ClassifierSchema}. The
 *       MetaPointer for each feature slot is stored once in the schema rather than once per
 *       instance, eliminating the dominant per-node overhead for large homogeneous models. Nodes
 *       whose features deviate from the schema are handled via optional overflow maps.
 *   <li><b>Legacy mode</b> (active when {@code schema} is null): features are stored in {@code
 *       ArrayList}s of wrapper objects ({@link SerializedPropertyValue}, {@link
 *       SerializedContainmentValue}, {@link SerializedReferenceValue}). This is the original
 *       representation, used for programmatically constructed nodes and all paths that do not go
 *       through the ProtoBuf compact deserialization.
 * </ol>
 *
 * <p>The public API is identical regardless of which mode is active. Methods such as {@link
 * #getProperties()}, {@link #addChild}, and {@link #setPropertyValue} work correctly in both modes.
 */
public class SerializedClassifierInstance {

  private String id;
  private MetaPointer classifier;
  private String parentNodeID;

  // ==========================================================================
  // SCHEMA MODE  (active when schema != null)
  // ==========================================================================
  //
  // In schema mode the "what feature is this?" metadata is stored once in the
  // shared ClassifierSchema rather than once per node.  Each array below is
  // parallel to the corresponding *Keys array in the schema:
  //   propertyValues[i]    ↔  schema.propertyKeys[i]
  //   containmentValues[i] ↔  schema.containmentKeys[i]
  //   referenceValues[i]   ↔  schema.referenceKeys[i]
  //
  // A null element means the feature was absent from the serialized data (i.e.
  // it was not included when serializeEmptyFeatures was false).
  //
  // ---------------------------------------------------------------------------

  private @Nullable ClassifierSchema schema;

  /** One slot per {@code schema.propertyKeys[i]}. Null means the property was absent. */
  private @Nullable String[] propertyValues;

  /**
   * One slot per {@code schema.containmentKeys[i]}. Null means the containment was absent from the
   * serialized data (no children and not serialized as an empty list).
   */
  @SuppressWarnings("unchecked")
  private @Nullable List<String>[] containmentValues;

  /**
   * One slot per {@code schema.referenceKeys[i]}. Null means the reference was absent from the
   * serialized data.
   */
  @SuppressWarnings("unchecked")
  private @Nullable List<SerializedReferenceValue.Entry>[] referenceValues;

  // Overflow maps: used for the rare case where a node has features that are not in its schema
  // (e.g. a broken node from an older model version or a hand-constructed test node).
  // These are null for the vast majority of nodes, so the cost is just one null-pointer check.

  private @Nullable Map<MetaPointer, String> overflowProperties;
  private @Nullable Map<MetaPointer, List<String>> overflowContainments;
  private @Nullable Map<MetaPointer, List<SerializedReferenceValue.Entry>> overflowReferences;

  // ==========================================================================
  // LEGACY MODE  (active when schema == null)
  // ==========================================================================

  /**
   * Given that in wide trees most nodes have few properties, we start with a small initial
   * capacity.
   */
  private final List<SerializedPropertyValue> legacyProperties = new ArrayList<>(5);

  /**
   * Null until the first containment is added. Given that in wide trees most nodes have no
   * containments, we avoid the instantiation unless necessary.
   */
  private @Nullable List<SerializedContainmentValue> legacyContainments;

  /** Null until the first reference is added. Most nodes have no outgoing references. */
  private @Nullable List<SerializedReferenceValue> legacyReferences;

  // ==========================================================================
  // ALWAYS USED
  // ==========================================================================

  /** Given most nodes have no annotations, we avoid the instantiation, unless it is necessary. */
  private @Nullable List<String> annotations;

  //
  // Constructors and factory methods
  //

  public SerializedClassifierInstance() {}

  public SerializedClassifierInstance(String id, MetaPointer concept) {
    setID(id);
    setClassifier(concept);
  }

  /**
   * Creates an instance in schema (compact) mode.
   *
   * <p>Called from the ProtoBuf deserialization hot path to bypass all intermediate wrapper-object
   * creation. The arrays passed in are owned by this instance from this point on; callers must not
   * retain references to them or mutate them externally.
   *
   * <p>None of the arrays are defensively copied. This is intentional: the deserialization path
   * allocates them fresh for each node, so there is no aliasing risk.
   *
   * @param schema the shared layout descriptor for this classifier; must not be null
   * @param id the node ID (may be null for broken nodes)
   * @param parentNodeID the parent's node ID (may be null for root nodes)
   * @param propertyValues one slot per {@code schema.propertyKeys[i]}; null element = absent
   * @param containmentValues one slot per {@code schema.containmentKeys[i]}; null = absent
   * @param referenceValues one slot per {@code schema.referenceKeys[i]}; null = absent
   */
  public static SerializedClassifierInstance compact(
      @Nonnull ClassifierSchema schema,
      @Nullable String id,
      @Nullable String parentNodeID,
      @Nonnull String[] propertyValues,
      @Nonnull List<String>[] containmentValues,
      @Nonnull List<SerializedReferenceValue.Entry>[] referenceValues) {
    SerializedClassifierInstance sci = new SerializedClassifierInstance();
    sci.schema = schema;
    sci.classifier = schema.classifier;
    sci.id = id;
    sci.parentNodeID = parentNodeID;
    sci.propertyValues = propertyValues;
    sci.containmentValues = containmentValues;
    sci.referenceValues = referenceValues;
    return sci;
  }

  //
  // Methods about parent
  //

  public String getParentNodeID() {
    return parentNodeID;
  }

  public void setParentNodeID(String parentNodeID) {
    this.parentNodeID = parentNodeID;
  }

  //
  // Methods about properties
  //

  /**
   * Returns an unmodifiable view of all properties.
   *
   * <p>In schema mode this returns a live view backed directly by the compact arrays — no
   * intermediate objects are allocated until an element is actually retrieved (the {@link
   * SerializedPropertyValue} flyweight is created on demand).
   */
  public List<SerializedPropertyValue> getProperties() {
    if (schema != null) {
      return new SchemaPropertyView();
    }
    return Collections.unmodifiableList(legacyProperties);
  }

  @Nullable
  public String getPropertyValue(String propertyKey) {
    if (schema != null) {
      // Linear scan by key string; MetaPointer.getKey() is a plain String field.
      for (int i = 0; i < schema.propertyKeys.length; i++) {
        if (schema.propertyKeys[i].getKey().equals(propertyKey)) return propertyValues[i];
      }
      if (overflowProperties != null) {
        for (Map.Entry<MetaPointer, String> e : overflowProperties.entrySet()) {
          if (e.getKey().getKey().equals(propertyKey)) return e.getValue();
        }
      }
      return null;
    }
    for (SerializedPropertyValue pv : legacyProperties) {
      if (pv.getMetaPointer().getKey().equals(propertyKey)) return pv.getValue();
    }
    return null;
  }

  @Nullable
  public String getPropertyValue(@Nonnull MetaPointer propertyMetaPointer) {
    if (schema != null) {
      // O(1) index lookup using identity comparison (MetaPointers are interned).
      int idx = schema.indexOfProperty(propertyMetaPointer);
      if (idx >= 0) return propertyValues[idx];
      return overflowProperties == null ? null : overflowProperties.get(propertyMetaPointer);
    }
    for (SerializedPropertyValue pv : legacyProperties) {
      if (propertyMetaPointer.equals(pv.getMetaPointer())) return pv.getValue();
    }
    return null;
  }

  /**
   * WARNING: this will always append the property, even if one entry with the same metapointer
   * already exists.
   *
   * <p>It is however slightly faster than the (safer) setPropertyValue.
   *
   * <p>In schema mode the MetaPointer is resolved to a schema slot (O(n) linear scan, n ≤ ~20); if
   * absent from the schema the value is placed in the overflow map.
   *
   * @param propertyValue the value should be non null to constitute a valid chunk, but a null value
   *     would not cause an error
   */
  public void unsafeAppendPropertyValue(@Nullable SerializedPropertyValue propertyValue) {
    if (schema != null) {
      // A null value has no MetaPointer to route it to a schema slot; silently drop it just as
      // the legacy path would on the next read (null entries are never returned from
      // getProperties).
      if (propertyValue == null) return;
      MetaPointer mp = propertyValue.getMetaPointer();
      int idx = mp != null ? schema.indexOfProperty(mp) : -1;
      if (idx >= 0) {
        propertyValues[idx] = propertyValue.getValue();
      } else if (mp != null) {
        initOverflowProperties().put(mp, propertyValue.getValue());
      }
      return;
    }
    legacyProperties.add(propertyValue);
  }

  /**
   * Updates or adds a {@link SerializedPropertyValue} in the properties list. If a property with
   * the same MetaPointer already exists, it is replaced. Otherwise, the property is appended to the
   * list.
   *
   * @param propertyValue the serialized property value to set; must not be null
   */
  public void setPropertyValue(@Nonnull SerializedPropertyValue propertyValue) {
    Objects.requireNonNull(propertyValue, "propertyValue must not be null");
    if (schema != null) {
      MetaPointer mp = propertyValue.getMetaPointer();
      int idx = mp != null ? schema.indexOfProperty(mp) : -1;
      if (idx >= 0) {
        propertyValues[idx] = propertyValue.getValue();
      } else if (mp != null) {
        initOverflowProperties().put(mp, propertyValue.getValue());
      }
      return;
    }
    for (int i = 0; i < legacyProperties.size(); i++) {
      SerializedPropertyValue existing = legacyProperties.get(i);
      if (existing.getMetaPointer() != null
          && existing.getMetaPointer().equals(propertyValue.getMetaPointer())) {
        legacyProperties.set(i, propertyValue);
        return;
      }
    }
    legacyProperties.add(propertyValue);
  }

  /**
   * Updates or adds a {@link SerializedPropertyValue} in the properties list. If a property with
   * the same {@link MetaPointer} already exists, it is replaced. Otherwise, the property is
   * appended to the list.
   *
   * @param propertyMetaPointer the metadata pointer identifying the property; may be null
   * @param serializedValue the serialized value of the property; may be null
   */
  public void setPropertyValue(
      @Nullable MetaPointer propertyMetaPointer, @Nullable String serializedValue) {
    setPropertyValue(SerializedPropertyValue.get(propertyMetaPointer, serializedValue));
  }

  //
  // Methods about containments
  //

  /**
   * Returns an unmodifiable view of all containments.
   *
   * <p>In schema mode only non-null slots (features that were present in the serialized data) are
   * included. The returned {@link SerializedContainmentValue} wrappers are constructed on demand
   * around the live lists in the compact array; no list copies are made.
   */
  public List<SerializedContainmentValue> getContainments() {
    if (schema != null) {
      return new SchemaContainmentView();
    }
    if (legacyContainments == null) return Collections.emptyList();
    return Collections.unmodifiableList(legacyContainments);
  }

  public List<String> getChildren() {
    if (schema != null) {
      List<String> children = new ArrayList<>();
      for (List<String> cv : containmentValues) {
        if (cv != null) children.addAll(cv);
      }
      if (overflowContainments != null) {
        for (List<String> cv : overflowContainments.values()) children.addAll(cv);
      }
      return Collections.unmodifiableList(children);
    }
    if (legacyContainments == null) return Collections.emptyList();
    List<String> children = new ArrayList<>();
    legacyContainments.forEach(c -> children.addAll(c.getChildrenIds()));
    return Collections.unmodifiableList(children);
  }

  @Nonnull
  public List<String> getContainmentValues(@Nonnull MetaPointer containmentMetaPointer) {
    if (schema != null) {
      // O(1) lookup for the common case; overflow is O(1) HashMap get.
      int idx = schema.indexOfContainment(containmentMetaPointer);
      if (idx >= 0) {
        List<String> cv = containmentValues[idx];
        return cv == null ? Collections.emptyList() : Collections.unmodifiableList(cv);
      }
      if (overflowContainments != null) {
        List<String> cv = overflowContainments.get(containmentMetaPointer);
        return cv == null ? Collections.emptyList() : Collections.unmodifiableList(cv);
      }
      return Collections.emptyList();
    }
    for (SerializedContainmentValue cv : getContainments()) {
      if (containmentMetaPointer.equals(cv.getMetaPointer())) {
        return Collections.unmodifiableList(cv.getChildrenIds());
      }
    }
    return Collections.emptyList();
  }

  /**
   * WARNING: this will always append the containment, even if one entry with the same metapointer
   * already exists.
   *
   * <p>It is however slightly faster than the (safer) addChild.
   */
  public void unsafeAppendContainmentValue(SerializedContainmentValue containmentValue) {
    if (schema != null) {
      MetaPointer mp = containmentValue.getMetaPointer();
      int idx = mp != null ? schema.indexOfContainment(mp) : -1;
      if (idx >= 0) {
        // Replace the slot entirely; the incoming list is copied so subsequent mutation of the
        // SerializedContainmentValue does not alias our compact storage.
        containmentValues[idx] = new ArrayList<>(containmentValue.getChildrenIds());
      } else if (mp != null) {
        initOverflowContainments().put(mp, new ArrayList<>(containmentValue.getChildrenIds()));
      }
      return;
    }
    initLegacyContainments();
    legacyContainments.add(containmentValue);
  }

  /**
   * Appends a new containment entry to the current instance. Always appends, regardless of whether
   * a similar containment already exists.
   *
   * @param containment the {@link MetaPointer} identifying the containment; may be null.
   * @param childrenIds a non-null list of child identifiers.
   */
  public void unsafeAppendContainmentValue(
      @Nullable MetaPointer containment, @Nonnull List<String> childrenIds) {
    if (schema != null) {
      if (containment == null) return;
      int idx = schema.indexOfContainment(containment);
      if (idx >= 0) {
        containmentValues[idx] = new ArrayList<>(childrenIds);
      } else {
        initOverflowContainments().put(containment, new ArrayList<>(childrenIds));
      }
      return;
    }
    initLegacyContainments();
    legacyContainments.add(new SerializedContainmentValue(containment, childrenIds));
  }

  public void addChild(@Nonnull MetaPointer metaPointer, @Nonnull String childID) {
    Objects.requireNonNull(metaPointer);
    Objects.requireNonNull(childID);
    if (schema != null) {
      childListFor(metaPointer).add(childID);
      return;
    }
    initLegacyContainments();
    Optional<SerializedContainmentValue> entry =
        legacyContainments.stream().filter(c -> c.getMetaPointer().equals(metaPointer)).findFirst();
    if (entry.isPresent()) {
      List<String> newValue = new ArrayList<>(entry.get().getChildrenIds());
      newValue.add(childID);
      entry.get().setChildrenIds(newValue);
    } else {
      unsafeAppendContainmentValue(metaPointer, Arrays.asList(childID));
    }
  }

  /**
   * Adds a child identifier at the specified index.
   *
   * @param metaPointer the containment reference; must not be null
   * @param childID the identifier of the child; must not be null
   * @param index the position where the childID should be inserted; must be non-negative
   * @throws NullPointerException if {@code metaPointer} or {@code childID} is null
   * @throws IllegalArgumentException if {@code index} is less than zero
   */
  public void addChild(@Nonnull MetaPointer metaPointer, @Nonnull String childID, int index) {
    Objects.requireNonNull(metaPointer, "metaPointer should not be null");
    Objects.requireNonNull(childID, "childId should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("Index must be greater than or equal to zero");
    }
    if (schema != null) {
      List<String> list = childListFor(metaPointer);
      if (index > list.size()) {
        throw new IllegalStateException("Index 0.." + list.size() + " expected, but got " + index);
      }
      list.add(index, childID);
      return;
    }
    initLegacyContainments();
    Optional<SerializedContainmentValue> entry =
        legacyContainments.stream().filter(c -> c.getMetaPointer().equals(metaPointer)).findFirst();
    if (entry.isPresent()) {
      List<String> newValue = new ArrayList<>(entry.get().getChildrenIds());
      newValue.add(index, childID);
      entry.get().setChildrenIds(newValue);
    } else {
      unsafeAppendContainmentValue(metaPointer, Arrays.asList(childID));
    }
  }

  public boolean removeContainmentValue(@Nonnull MetaPointer metaPointer) {
    Objects.requireNonNull(metaPointer);
    if (schema != null) {
      int idx = schema.indexOfContainment(metaPointer);
      if (idx >= 0 && containmentValues[idx] != null) {
        containmentValues[idx] = null; // null = absent; the slot stays in the schema
        return true;
      }
      if (overflowContainments != null) {
        return overflowContainments.remove(metaPointer) != null;
      }
      return false;
    }
    if (legacyContainments == null) return false;
    return legacyContainments.removeIf(c -> c.getMetaPointer().equals(metaPointer));
  }

  /**
   * Removes the specified childId from the containments, if present.
   *
   * @param childId the identifier of the childId to be removed; must not be null
   * @return true if the childId was successfully removed, false otherwise
   */
  public boolean removeChild(@Nonnull String childId) {
    Objects.requireNonNull(childId, "childId should not be null");
    if (schema != null) {
      for (List<String> cv : containmentValues) {
        if (cv != null && cv.remove(childId)) return true;
      }
      if (overflowContainments != null) {
        for (List<String> cv : overflowContainments.values()) {
          if (cv.remove(childId)) return true;
        }
      }
      return false;
    }
    if (legacyContainments == null) return false;
    for (SerializedContainmentValue containment : legacyContainments) {
      if (containment.removeChild(childId)) return true;
    }
    return false;
  }

  /**
   * Remove all containments. This is useful when we want to create a partition, as they cannot be
   * created with children. Children can only be added in a second moment.
   */
  public void clearContainments() {
    if (schema != null) {
      // Reset all schema slots to absent; overflow is also cleared.
      Arrays.fill(containmentValues, null);
      overflowContainments = null;
      return;
    }
    legacyContainments = null;
  }

  //
  // Methods about references
  //

  /**
   * Returns an unmodifiable view of all references.
   *
   * <p>In schema mode only non-null slots are included. Wrappers are created on demand.
   */
  public List<SerializedReferenceValue> getReferences() {
    if (schema != null) {
      return new SchemaReferenceView();
    }
    if (legacyReferences == null) return Collections.emptyList();
    return Collections.unmodifiableList(legacyReferences);
  }

  @Nullable
  public List<SerializedReferenceValue.Entry> getReferenceValues(String referenceKey) {
    if (schema != null) {
      for (int i = 0; i < schema.referenceKeys.length; i++) {
        if (schema.referenceKeys[i].getKey().equals(referenceKey)) {
          return referenceValues[i] == null
              ? null
              : Collections.unmodifiableList(referenceValues[i]);
        }
      }
      if (overflowReferences != null) {
        for (Map.Entry<MetaPointer, List<SerializedReferenceValue.Entry>> e :
            overflowReferences.entrySet()) {
          if (e.getKey().getKey().equals(referenceKey)) {
            return Collections.unmodifiableList(e.getValue());
          }
        }
      }
      return null;
    }
    for (SerializedReferenceValue rv : getReferences()) {
      if (rv.getMetaPointer().getKey().equals(referenceKey)) {
        return Collections.unmodifiableList(rv.getValue());
      }
    }
    return null;
  }

  @Nonnull
  public List<SerializedReferenceValue.Entry> getReferenceValues(
      @Nonnull MetaPointer referenceMetaPointer) {
    if (schema != null) {
      int idx = schema.indexOfReference(referenceMetaPointer);
      if (idx >= 0) {
        return referenceValues[idx] == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(referenceValues[idx]);
      }
      if (overflowReferences != null) {
        List<SerializedReferenceValue.Entry> entries = overflowReferences.get(referenceMetaPointer);
        return entries == null ? Collections.emptyList() : Collections.unmodifiableList(entries);
      }
      return Collections.emptyList();
    }
    for (SerializedReferenceValue rv : getReferences()) {
      if (referenceMetaPointer.equals(rv.getMetaPointer())) {
        return Collections.unmodifiableList(rv.getValue());
      }
    }
    return Collections.emptyList();
  }

  /**
   * WARNING: this will always append the containment, even if one entry with the same metapointer
   * already exists.
   *
   * <p>It is however slightly faster than the (safer) addReferenceValue.
   *
   * @param referenceValue the value should be non null to constitute a valid chunk, but a null
   *     value would not cause an error
   */
  public void unsafeAppendReferenceValue(@Nullable SerializedReferenceValue referenceValue) {
    if (schema != null) {
      if (referenceValue == null) return;
      MetaPointer mp = referenceValue.getMetaPointer();
      if (mp == null) return;
      int idx = schema.indexOfReference(mp);
      if (idx >= 0) {
        referenceValues[idx] = new ArrayList<>(referenceValue.getValue());
      } else {
        initOverflowReferences().put(mp, new ArrayList<>(referenceValue.getValue()));
      }
      return;
    }
    initLegacyReferences();
    legacyReferences.add(referenceValue);
  }

  /**
   * Adds a reference value associated with the specified MetaPointer. If a reference with the given
   * MetaPointer already exists, the new reference value is added to the existing list of entries.
   * Otherwise, a new reference entry is created.
   *
   * @param metaPointer the MetaPointer instance identifying the reference; must not be null
   * @param referenceValue the reference value entry to be added; must not be null
   */
  public void addReferenceValue(
      @Nonnull MetaPointer metaPointer, @Nonnull SerializedReferenceValue.Entry referenceValue) {
    Objects.requireNonNull(metaPointer);
    Objects.requireNonNull(referenceValue);
    if (schema != null) {
      refListFor(metaPointer).add(referenceValue);
      return;
    }
    initLegacyReferences();
    Optional<SerializedReferenceValue> entry =
        legacyReferences.stream().filter(c -> c.getMetaPointer().equals(metaPointer)).findFirst();
    if (entry.isPresent()) {
      List<SerializedReferenceValue.Entry> newValue = new ArrayList<>(entry.get().getValue());
      newValue.add(referenceValue);
      entry.get().setValue(newValue);
    } else {
      unsafeAppendReferenceValue(
          new SerializedReferenceValue(metaPointer, Arrays.asList(referenceValue)));
    }
  }

  /**
   * Adds a reference value at the specified index.
   *
   * @param metaPointer the MetaPointer identifying the reference; must not be null
   * @param index the position in the reference value list; must be &gt;= 0
   * @param referenceValue the reference value entry to add; must not be null
   * @throws IllegalArgumentException if index &lt; 0
   * @throws IllegalStateException if index exceeds the current list size
   * @throws NullPointerException if metaPointer or referenceValue is null
   */
  public void addReferenceValue(
      @Nonnull MetaPointer metaPointer,
      int index,
      @Nonnull SerializedReferenceValue.Entry referenceValue) {
    if (index < 0) {
      throw new IllegalArgumentException("Index must be greater than or equal to zero");
    }
    Objects.requireNonNull(metaPointer);
    Objects.requireNonNull(referenceValue);
    if (schema != null) {
      List<SerializedReferenceValue.Entry> list = refListFor(metaPointer);
      if (index > list.size()) {
        throw new IllegalStateException("Index 0.." + list.size() + " expected, but got " + index);
      }
      list.add(index, referenceValue);
      return;
    }
    initLegacyReferences();
    Optional<SerializedReferenceValue> entry =
        legacyReferences.stream().filter(c -> c.getMetaPointer().equals(metaPointer)).findFirst();
    if (entry.isPresent()) {
      List<SerializedReferenceValue.Entry> newValue = new ArrayList<>(entry.get().getValue());
      if (index > newValue.size()) {
        throw new IllegalStateException(
            "Index 0.." + newValue.size() + " expected, but got " + index);
      }
      newValue.add(index, referenceValue);
      entry.get().setValue(newValue);
    } else {
      if (index > 0) {
        throw new IllegalStateException("Index 0..0 expected, but got " + index);
      }
      unsafeAppendReferenceValue(
          new SerializedReferenceValue(metaPointer, Arrays.asList(referenceValue)));
    }
  }

  public void setReferenceValue(
      MetaPointer reference, List<SerializedReferenceValue.Entry> referenceValues) {
    setReferenceValue(new SerializedReferenceValue(reference, referenceValues));
  }

  public void setReferenceValue(
      MetaPointer reference, SerializedReferenceValue.Entry... referenceValues) {
    setReferenceValue(new SerializedReferenceValue(reference, Arrays.asList(referenceValues)));
  }

  public void setReferenceValue(@Nonnull SerializedReferenceValue referenceValue) {
    Objects.requireNonNull(referenceValue);
    if (schema != null) {
      MetaPointer mp = referenceValue.getMetaPointer();
      if (mp == null) return;
      int idx = schema.indexOfReference(mp);
      if (idx >= 0) {
        this.referenceValues[idx] = new ArrayList<>(referenceValue.getValue());
      } else {
        initOverflowReferences().put(mp, new ArrayList<>(referenceValue.getValue()));
      }
      return;
    }
    initLegacyReferences();
    for (int i = 0; i < legacyReferences.size(); i++) {
      SerializedReferenceValue entry = legacyReferences.get(i);
      if (entry.getMetaPointer() != null
          && entry.getMetaPointer().equals(referenceValue.getMetaPointer())) {
        legacyReferences.set(i, referenceValue);
        return;
      }
    }
    unsafeAppendReferenceValue(referenceValue);
  }

  //
  // Methods about annotations
  //

  public List<String> getAnnotations() {
    if (this.annotations == null) return Collections.emptyList();
    return Collections.unmodifiableList(this.annotations);
  }

  public void setAnnotations(@Nonnull List<String> annotationIDs) {
    Objects.requireNonNull(annotationIDs, "annotationIDs should not be null");
    if (this.annotations == null) {
      this.annotations = new ArrayList<>(annotationIDs.size());
    } else {
      this.annotations.clear();
    }
    this.annotations.addAll(annotationIDs);
  }

  public void addAnnotation(String annotationID) {
    if (this.annotations == null) this.annotations = new ArrayList<>(1);
    this.annotations.add(annotationID);
  }

  /**
   * Removes the specified annotation identified by its ID from the list of annotations.
   *
   * @param annotationID the ID of the annotation to be removed; must not be null
   * @return true if the annotation was successfully removed, otherwise false
   */
  public boolean removeAnnotation(@Nonnull String annotationID) {
    Objects.requireNonNull(annotationID, "annotationID must not be null");
    if (this.annotations == null) return false;
    return this.annotations.remove(annotationID);
  }

  //
  // Other methods
  //

  public MetaPointer getClassifier() {
    return classifier;
  }

  public void setClassifier(MetaPointer classifier) {
    this.classifier = classifier;
  }

  @Nullable
  public String getID() {
    return id;
  }

  public void setID(String id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedClassifierInstance)) return false;
    SerializedClassifierInstance that = (SerializedClassifierInstance) o;
    // Use public getters so both modes produce comparable Lists.
    return Objects.equals(id, that.id)
        && Objects.equals(classifier, that.classifier)
        && Objects.equals(parentNodeID, that.parentNodeID)
        && Objects.equals(getProperties(), that.getProperties())
        && Objects.equals(getContainments(), that.getContainments())
        && Objects.equals(getReferences(), that.getReferences())
        && Objects.equals(getAnnotations(), that.getAnnotations());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        classifier,
        parentNodeID,
        getProperties(),
        getContainments(),
        getReferences(),
        getAnnotations());
  }

  @Override
  public String toString() {
    return "SerializedClassifierInstance{"
        + "id='"
        + id
        + '\''
        + ", classifier="
        + classifier
        + ", parentNodeID='"
        + parentNodeID
        + '\''
        + ", properties="
        + getProperties()
        + ", containments="
        + getContainments()
        + ", references="
        + getReferences()
        + ", annotations="
        + getAnnotations()
        + '}';
  }

  /**
   * Checks whether the specified identifier is contained in the list of containments or annotations
   * associated with this instance.
   *
   * @param id the identifier to check for containment; must not be null
   * @return true if the identifier is found in either the containments or annotations, false
   *     otherwise
   */
  public boolean contains(@Nonnull String id) {
    Objects.requireNonNull(id, "id must not be null");
    if (schema != null) {
      for (List<String> cv : containmentValues) {
        if (cv != null && cv.contains(id)) return true;
      }
      if (overflowContainments != null) {
        for (List<String> cv : overflowContainments.values()) {
          if (cv.contains(id)) return true;
        }
      }
      if (annotations != null) return annotations.contains(id);
      return false;
    }
    if (this.legacyContainments != null) {
      for (SerializedContainmentValue containmentValue : legacyContainments) {
        for (String childId : containmentValue.getChildrenIds()) {
          if (Objects.equals(childId, id)) return true;
        }
      }
    }
    if (this.annotations != null) return this.annotations.contains(id);
    return false;
  }

  //
  // Private helpers — schema mode
  //

  /**
   * Returns the live {@code List<String>} for the given containment MetaPointer in schema mode,
   * creating it if absent. Routes to the overflow map when the MetaPointer is not in the schema.
   *
   * <p>This helper exists so that {@link #addChild} and {@link #addChild(MetaPointer, String, int)}
   * can share a single code path.
   */
  private List<String> childListFor(MetaPointer metaPointer) {
    int idx = schema.indexOfContainment(metaPointer);
    if (idx >= 0) {
      if (containmentValues[idx] == null) containmentValues[idx] = new ArrayList<>();
      return containmentValues[idx];
    }
    return initOverflowContainments().computeIfAbsent(metaPointer, k -> new ArrayList<>());
  }

  /**
   * Returns the live entry list for the given reference MetaPointer in schema mode, creating it if
   * absent. Routes to the overflow map when the MetaPointer is not in the schema.
   */
  private List<SerializedReferenceValue.Entry> refListFor(MetaPointer metaPointer) {
    int idx = schema.indexOfReference(metaPointer);
    if (idx >= 0) {
      if (referenceValues[idx] == null) referenceValues[idx] = new ArrayList<>(1);
      return referenceValues[idx];
    }
    return initOverflowReferences().computeIfAbsent(metaPointer, k -> new ArrayList<>(1));
  }

  private Map<MetaPointer, String> initOverflowProperties() {
    if (overflowProperties == null) overflowProperties = new HashMap<>(2);
    return overflowProperties;
  }

  private Map<MetaPointer, List<String>> initOverflowContainments() {
    if (overflowContainments == null) overflowContainments = new HashMap<>(2);
    return overflowContainments;
  }

  private Map<MetaPointer, List<SerializedReferenceValue.Entry>> initOverflowReferences() {
    if (overflowReferences == null) overflowReferences = new HashMap<>(2);
    return overflowReferences;
  }

  //
  // Private helpers — legacy mode
  //

  private void initLegacyReferences() {
    if (legacyReferences == null) legacyReferences = new ArrayList<>(1);
  }

  private void initLegacyContainments() {
    if (legacyContainments == null) legacyContainments = new ArrayList<>(3);
  }

  // ==========================================================================
  // Schema-backed views (inner classes)
  //
  // These AbstractList subclasses avoid allocating an intermediate ArrayList;
  // elements are constructed on demand directly from the compact arrays.
  // The views are unmodifiable (AbstractList's set/add/remove throw
  // UnsupportedOperationException by default).
  // ==========================================================================

  /**
   * Live view over {@code propertyValues[]} + {@code overflowProperties}.
   *
   * <p>Iterating this view creates {@link SerializedPropertyValue} flyweight objects on demand via
   * {@link SerializedPropertyValue#get}; common short values (booleans, small integers) hit the
   * flyweight cache and do not allocate.
   */
  private final class SchemaPropertyView extends AbstractList<SerializedPropertyValue> {

    @Override
    public int size() {
      return schema.propertyCount() + (overflowProperties == null ? 0 : overflowProperties.size());
    }

    @Override
    public SerializedPropertyValue get(int index) {
      int base = schema.propertyCount();
      if (index < base) {
        return SerializedPropertyValue.get(schema.propertyKeys[index], propertyValues[index]);
      }
      if (overflowProperties != null) {
        int i = index - base;
        for (Map.Entry<MetaPointer, String> e : overflowProperties.entrySet()) {
          if (i-- == 0) return SerializedPropertyValue.get(e.getKey(), e.getValue());
        }
      }
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
    }
  }

  /**
   * Live view over non-null slots in {@code containmentValues[]} + {@code overflowContainments}.
   *
   * <p>Null slots correspond to containments that were absent from the serialized data (i.e. not
   * emitted when {@code serializeEmptyFeatures} was false). They are skipped, so the view length
   * may be smaller than {@code schema.containmentCount()}.
   *
   * <p>The returned {@link SerializedContainmentValue} wrappers are constructed with the no-copy
   * package-private constructor so the caller's list and our compact storage share the same backing
   * list. Mutations on the returned wrapper flow back into compact storage.
   */
  private final class SchemaContainmentView extends AbstractList<SerializedContainmentValue> {

    @Override
    public int size() {
      int count = 0;
      for (List<String> cv : containmentValues) {
        if (cv != null) count++;
      }
      if (overflowContainments != null) count += overflowContainments.size();
      return count;
    }

    @Override
    public SerializedContainmentValue get(int index) {
      if (index < 0) throw new IndexOutOfBoundsException("Index: " + index);
      int pos = 0;
      for (int i = 0; i < containmentValues.length; i++) {
        if (containmentValues[i] != null) {
          if (pos == index) {
            // No-copy constructor: the wrapper shares the live list in compact storage.
            return new SerializedContainmentValue(
                schema.containmentKeys[i], containmentValues[i], true);
          }
          pos++;
        }
      }
      if (overflowContainments != null) {
        for (Map.Entry<MetaPointer, List<String>> e : overflowContainments.entrySet()) {
          if (pos == index) {
            return new SerializedContainmentValue(e.getKey(), e.getValue(), true);
          }
          pos++;
        }
      }
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
    }
  }

  /**
   * Live view over non-null slots in {@code referenceValues[]} + {@code overflowReferences}.
   *
   * <p>The returned {@link SerializedReferenceValue} wrappers are constructed with the no-copy
   * package-private constructor to avoid defensive list copies.
   */
  private final class SchemaReferenceView extends AbstractList<SerializedReferenceValue> {

    @Override
    public int size() {
      int count = 0;
      for (List<SerializedReferenceValue.Entry> rv : referenceValues) {
        if (rv != null) count++;
      }
      if (overflowReferences != null) count += overflowReferences.size();
      return count;
    }

    @Override
    public SerializedReferenceValue get(int index) {
      if (index < 0) throw new IndexOutOfBoundsException("Index: " + index);
      int pos = 0;
      for (int i = 0; i < referenceValues.length; i++) {
        if (referenceValues[i] != null) {
          if (pos == index) {
            return new SerializedReferenceValue(schema.referenceKeys[i], referenceValues[i], true);
          }
          pos++;
        }
      }
      if (overflowReferences != null) {
        for (Map.Entry<MetaPointer, List<SerializedReferenceValue.Entry>> e :
            overflowReferences.entrySet()) {
          if (pos == index) {
            return new SerializedReferenceValue(e.getKey(), e.getValue(), true);
          }
          pos++;
        }
      }
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
    }
  }
}
