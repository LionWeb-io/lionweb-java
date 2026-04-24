package io.lionweb.serialization.data;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Lower level representation of a Classifier Instance (either a Node or an AnnotationInstance)
 * which is used to load classifier instances during serialization. Note that also "broken"
 * classifier instances can be loaded.
 */
public class SerializedClassifierInstance {
  private String id;
  //private MetaPointer classifier;
  private Schema schema;

  //private final List<SerializedPropertyValue> properties = new ArrayList<>();
  private final String[] properties;
  private final List<String>[] containments;
  private final List<SerializedReferenceValue.Entry>[] references;

  /**
   * Given that in wide trees most nodes have no containments, we avoid the instantiation, unless it
   * is necessary.
   */
  //private @Nullable List<SerializedContainmentValue> containments;

  /** Given most nodes have no references, we avoid the instantiation, unless it is necessary. */
  //private @Nullable List<SerializedReferenceValue> references;

  /** Given most nodes have no annotations, we avoid the instantiation, unless it is necessary. */
  private @Nullable List<String> annotations;

  private String parentNodeID;

  //
  // Constructors
  //
  public SerializedClassifierInstance(Schema schema) {
      this.schema = schema;
      this.properties = new String[schema.properties.length];
      this.containments = new List[schema.containments.length];
      this.references = new List[schema.references.length];
  }

    public SerializedClassifierInstance(String id, MetaPointer classifier) {
      this(id, Schema.fromMetaPointer(classifier));
    }

  public SerializedClassifierInstance(String id, Schema schema) {
    setID(id);
      this.schema = schema;
      this.properties = new String[schema.properties.length];
      this.containments = new List[schema.containments.length];
      this.references = new List[schema.references.length];
    //setClassifier(concept);
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

  public List<SerializedPropertyValue> getProperties() {
      List<SerializedPropertyValue> properties = new ArrayList<>(schema.properties.length);
      for (int i = 0; i < schema.properties.length; i++) {
          properties.add(SerializedPropertyValue.get(schema.properties[i], this.properties[i]));
      }
    return properties;
  }

  @Nullable
  public String getPropertyValue(String propertyKey) {
    for (SerializedPropertyValue pv : this.getProperties()) {
      if (pv.getMetaPointer().getKey().equals(propertyKey)) {
        return pv.getValue();
      }
    }
    return null;
  }

  @Nullable
  public String getPropertyValue(@Nonnull MetaPointer propertyMetaPointer) {
    for (SerializedPropertyValue pv : this.getProperties()) {
      if (propertyMetaPointer.equals(pv.getMetaPointer())) {
        return pv.getValue();
      }
    }
    return null;
  }

  /**
   * WARNING: this will always append the property, even if one entry with the same metapointer
   * already exists.
   *
   * <p>It is however slightly faster than the (safer) setPropertyValue.
   *
   * @param propertyValue the value should be non null to constitute a valid chunk, but a null value
   *     would not cause an error
   */
  public void unsafeAppendPropertyValue(@Nullable SerializedPropertyValue propertyValue) {
      if (propertyValue != null) {
          setPropertyValue(propertyValue);
      }
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
      for (int i = 0; i < schema.properties.length; i++) {
          if (schema.properties[i].equals(propertyValue.getMetaPointer())) {
              this.properties[i] = propertyValue.getValue();
              return;
          }
      }
      throw new IllegalArgumentException("Property not found: " + propertyValue.getMetaPointer());
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

  public List<SerializedContainmentValue> getContainments() {
      List<SerializedContainmentValue> containments = new ArrayList<>(schema.containments.length);
      for (int i = 0; i < schema.containments.length; i++) {
          containments.add(new SerializedContainmentValue(schema.containments[i], this.containments[i]));
      }
      return containments;
  }

  public List<String> getChildren() {
    List<String> children = new ArrayList<>();
    for (int i = 0; i < containments.length; i++) {
        children.addAll(containments[i]);
    }
    return children;
  }

  @Nonnull
  public List<String> getContainmentValues(@Nonnull MetaPointer containmentMetaPointer) {
    for (SerializedContainmentValue cv : this.getContainments()) {
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
    //initContainments();
      for (int i = 0; i < schema.containments.length; i++) {
          if (schema.containments[i].equals(containmentValue.getMetaPointer())) {
              this.containments[i] = containmentValue.getChildrenIds();
              return;
          }
      }
      throw new IllegalArgumentException("Containment not found: " + containmentValue.getMetaPointer());
  }

  /**
   * Appends a new containment entry to the current instance. The method adds a new {@link
   * SerializedContainmentValue} constructed with the provided containment reference and list of
   * child identifiers to the internal containments list. This operation always appends the entry,
   * regardless of whether a similar containment already exists.
   *
   * @param containment the {@link MetaPointer} identifying the containment; may be null to indicate
   *     no specific containment.
   * @param childrenIds a non-null list of child identifiers to be associated with the new
   *     containment value.
   */
  public void unsafeAppendContainmentValue(
      @Nullable MetaPointer containment, @Nonnull List<String> childrenIds) {
    //initContainments();
      if (containment != null) {
          for (int i = 0; i < schema.containments.length; i++) {
              if (schema.containments[i].equals(containment)) {
                  this.containments[i] = childrenIds;
                  return;
              }
          }
          throw new IllegalArgumentException("Containment not found: " + containment);
      }
  }

  public void addChild(@Nonnull MetaPointer metaPointer, @Nonnull String childID) {
    Objects.requireNonNull(metaPointer);
    Objects.requireNonNull(childID);
      for (int i = 0; i < schema.containments.length; i++) {
          if (schema.containments[i].equals(metaPointer)) {
              this.containments[i].add(childID);
              return;
          }
      }
      throw new IllegalArgumentException("Containment not found: " + metaPointer);
  }

  /**
   * Adds a child identifier to the containment list associated with the specified {@link
   * MetaPointer}. If a containment entry for the given {@code metaPointer} already exists, the
   * {@code childID} is inserted at the specified {@code index} in the list of children. If no such
   * entry exists, a new containment entry is created and appended.
   *
   * @param metaPointer the {@link MetaPointer} representing the containment reference; must not be
   *     null
   * @param childID the identifier of the child to be added; must not be null
   * @param index the position where the childID should be inserted; must be a non-negative integer
   * @throws NullPointerException if {@code metaPointer} or {@code childID} is null
   * @throws IllegalArgumentException if {@code index} is less than zero
   */
  public void addChild(@Nonnull MetaPointer metaPointer, @Nonnull String childID, int index) {
    Objects.requireNonNull(metaPointer, "metaPointer should not be null");
    Objects.requireNonNull(childID, "childId should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("Index must be greater than or equal to zero");
    }
      for (int i = 0; i < schema.containments.length; i++) {
          if (schema.containments[i].equals(metaPointer)) {
              this.containments[i].add(index, childID);
              return;
          }
      }
      throw new IllegalArgumentException("Containment not found: " + metaPointer);
  }

  public boolean removeContainmentValue(@Nonnull MetaPointer metaPointer) {
    Objects.requireNonNull(metaPointer);
      for (int i = 0; i < schema.containments.length; i++) {
          if (schema.containments[i].equals(metaPointer)) {
              boolean res = this.containments[i].size() > 0;
              this.containments[i].clear();
              return res;
          }
      }
      throw new IllegalArgumentException("Containment not found: " + metaPointer);
  }

  /**
   * Removes the specified childId from the containments, if present.
   *
   * @param childId the identifier of the childId to be removed; must not be null
   * @return true if the childId was successfully removed, false otherwise
   */
  public boolean removeChild(@Nonnull String childId) {
    Objects.requireNonNull(childId, "childId should not be null");
      for (int i = 0; i < schema.containments.length; i++) {
          boolean removed = this.containments[i].remove(childId);
          if (removed) {
              return true;
          }
      }
      return false;
  }

  /**
   * Remove all containments. This is useful when we want to create a partition, as they cannot be
   * created with children. Children can only be added in a second moment.
   */
  public void clearContainments() {
    for (int i = 0; i < schema.containments.length; i++) {
        this.containments[i].clear();
    }
  }

  //
  // Methods about references
  //

  public List<SerializedReferenceValue> getReferences() {
      List<SerializedReferenceValue> references = new ArrayList<>(schema.references.length);
      for (int i = 0; i < schema.references.length; i++) {
          references.add(new SerializedReferenceValue(schema.references[i], this.references[i]));
      }
      return references;
  }

  @Nullable
  public List<SerializedReferenceValue.Entry> getReferenceValues(String referenceKey) {
    for (SerializedReferenceValue rv : this.getReferences()) {
      if (rv.getMetaPointer().getKey().equals(referenceKey)) {
        return Collections.unmodifiableList(rv.getValue());
      }
    }
    return null;
  }

  @Nonnull
  public List<SerializedReferenceValue.Entry> getReferenceValues(
      @Nonnull MetaPointer referenceMetaPointer) {
    for (SerializedReferenceValue rv : this.getReferences()) {
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
    //initReferences();
    //this.references.add(referenceValue);
      for (int i = 0; i < schema.references.length; i++) {
          if (schema.references[i].equals(referenceValue.getMetaPointer())) {
              if (references[i] == null) {
                  references[i] = new ArrayList<>();
              }
              references[i].addAll(referenceValue.getValue());
              return;
          }
      }
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
    for (int i = 0; i < schema.references.length; i++) {
        if (schema.references[i].equals(metaPointer)) {
            references[i].add(referenceValue);
            return;
        }
    }
  }

  /**
   * Adds a reference value to the collection of serialized reference values at the specified index
   * for the given meta pointer. If a serialized reference value associated with the provided meta
   * pointer already exists, the new reference value is inserted into the current list of reference
   * values at the specified index. If no such entry exists, a new entry is created and the
   * reference value is appended.
   *
   * @param metaPointer The non-null meta pointer associated with the reference value to be added.
   * @param index The position in the current list of reference values where the new reference value
   *     should be inserted. Must be greater than or equal to zero.
   * @param referenceValue The non-null reference value to be added.
   * @throws IllegalArgumentException if the index is less than zero.
   * @throws IllegalStateException if the index is greater than the allowed range for current
   *     values.
   * @throws NullPointerException if metaPointer or referenceValue is null.
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
      for (int i = 0; i < schema.references.length; i++) {
          if (schema.references[i].equals(metaPointer)) {
              references[i].add(index, referenceValue);
              return;
          }
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
      for (int i = 0; i < schema.references.length; i++) {
          if (schema.references[i].equals(referenceValue.getMetaPointer())) {
              references[i] = referenceValue.getValue();
              return;
          }
      }
  }

  //
  // Methods about annotations
  //

  public List<String> getAnnotations() {
    if (this.annotations == null) {
      return Collections.emptyList();
    }
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
    if (this.annotations == null) {
      this.annotations = new ArrayList<>(1);
    }
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
    if (this.annotations == null) {
      return false;
    }
    return this.annotations.remove(annotationID);
  }

  //
  // Other methods
  //

  public MetaPointer getClassifier() {
    return schema.classifier;
  }

  public void setClassifier(MetaPointer classifier) {
    if (classifier.equals(this.schema.classifier)) {
        return;
    }
    throw new IllegalArgumentException("Cannot change classifier");
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
    return Objects.equals(id, that.id)
        && Objects.equals(schema, that.schema)
        && Objects.equals(parentNodeID, that.parentNodeID)
        && Objects.equals(properties, that.properties)
        && Objects.equals(containments, that.containments)
        && Objects.equals(references, that.references)
        && Objects.equals(getAnnotations(), that.getAnnotations());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        schema,
        parentNodeID,
        properties,
        containments,
        references,
        getAnnotations());
  }

  @Override
  public String toString() {
    return "SerializedClassifierInstance{"
        + "id='"
        + id
        + '\''
        + ", classifier="
        + schema.classifier
        + ", parentNodeID='"
        + parentNodeID
        + '\''
        + ", properties="
        + properties
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
    if (this.containments != null) {
      for (int i = 0; i < this.containments.length; i++) {
          if (this.containments[i].contains(id)) {
              return true;
          }
      }
    }
    if (this.annotations != null) {
      return this.annotations.contains(id);
    }
    return false;
  }

  //
  // Private methods
  //

//  private void initReferences() {
//    if (this.references == null) {
//      this.references = new ArrayList<>(1);
//    }
//  }
//
//  private void initContainments() {
//    if (this.containments == null) {
//      this.containments = new ArrayList<>();
//    }
//  }
}
