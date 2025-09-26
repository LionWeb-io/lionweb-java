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
  private MetaPointer classifier;

  private final List<SerializedPropertyValue> properties = new ArrayList<>();

  /**
   * Given that in wide trees most nodes have no containments, we avoid the instantiation, unless it
   * is necessary.
   */
  private @Nullable List<SerializedContainmentValue> containments;

  /** Given most nodes have no references, we avoid the instantiation, unless it is necessary. */
  private @Nullable List<SerializedReferenceValue> references;

  /** Given most nodes have no annotations, we avoid the instantiation, unless it is necessary. */
  private @Nullable List<String> annotations;

  private String parentNodeID;

  public String getParentNodeID() {
    return parentNodeID;
  }

  public void setParentNodeID(String parentNodeID) {
    this.parentNodeID = parentNodeID;
  }

  public SerializedClassifierInstance() {}

  public SerializedClassifierInstance(String id, MetaPointer concept) {
    setID(id);
    setClassifier(concept);
  }

  /**
   * Remove all containments. This is useful when we want to create a partition, as they cannot be
   * created with children. Children can only be added in a second moment.
   */
  public void clearContainments() {
    containments = null;
  }

  public List<SerializedContainmentValue> getContainments() {
    if (containments == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(this.containments);
  }

  public List<String> getChildren() {
    if (containments == null) {
      return Collections.emptyList();
    }
    List<String> children = new ArrayList<>();
    this.containments.forEach(c -> children.addAll(c.getChildrenIds()));
    return Collections.unmodifiableList(children);
  }

  public List<SerializedReferenceValue> getReferences() {
    if (this.references == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(this.references);
  }

  public List<String> getAnnotations() {
    if (this.annotations == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(this.annotations);
  }

  public List<SerializedPropertyValue> getProperties() {
    return Collections.unmodifiableList(properties);
  }

  /**
   * WARNING: this will always add the property, even if one entry with the same metapointer already
   * exists.
   *
   * <p>It is however slightly faster than the (safer) setPropertyValue.
   */
  public void unsafeAddPropertyValue(SerializedPropertyValue propertyValue) {
    this.properties.add(propertyValue);
  }

  public void setPropertyValue(SerializedPropertyValue propertyValue) {
    for (int i = 0; i < this.properties.size(); i++) {
      SerializedPropertyValue property = this.properties.get(i);
      if (property.getMetaPointer() != null
          && property.getMetaPointer().equals(propertyValue.getValue())) {
        this.properties.set(i, propertyValue);
        return;
      }
    }
    this.properties.add(propertyValue);
  }

  public void addContainmentValue(SerializedContainmentValue containmentValue) {
    initContainments();
    this.containments.add(containmentValue);
  }

  public boolean removeContainmentValue(@Nonnull MetaPointer metaPointer) {
    Objects.requireNonNull(metaPointer);
    if (this.containments == null) {
      return false;
    }
    return this.containments.removeIf(c -> c.getMetaPointer().equals(metaPointer));
  }

  public void addChild(@Nonnull MetaPointer metaPointer, @Nonnull String childID) {
    Objects.requireNonNull(metaPointer);
    Objects.requireNonNull(childID);
    initContainments();
    Optional<SerializedContainmentValue> entry =
        this.containments.stream().filter(c -> c.getMetaPointer().equals(metaPointer)).findFirst();
    if (entry.isPresent()) {
      List<String> currValue = entry.get().getChildrenIds();
      List<String> newValue = new ArrayList<>(currValue.size() + 1);
      newValue.addAll(currValue);
      newValue.add(childID);
      entry.get().setChildrenIds(newValue);
    } else {
      addChildren(metaPointer, Arrays.asList(childID));
    }
  }

  public void addReferenceValue(SerializedReferenceValue referenceValue) {
    initReferences();
    this.references.add(referenceValue);
  }

  public void addReferenceValue(
      @Nonnull MetaPointer metaPointer, @Nonnull SerializedReferenceValue.Entry referenceValue) {
    Objects.requireNonNull(metaPointer);
    Objects.requireNonNull(referenceValue);
    initReferences();
    Optional<SerializedReferenceValue> entry =
        this.references.stream().filter(c -> c.getMetaPointer().equals(metaPointer)).findFirst();
    if (entry.isPresent()) {
      List<SerializedReferenceValue.Entry> currValue = entry.get().getValue();
      List<SerializedReferenceValue.Entry> newValue = new ArrayList<>(currValue.size() + 1);
      newValue.addAll(currValue);
      newValue.add(referenceValue);
      entry.get().setValue(newValue);
    } else {
      addReferenceValue(metaPointer, Arrays.asList(referenceValue));
    }
  }

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

  public void setPropertyValue(MetaPointer property, String serializedValue) {
    this.properties.add(SerializedPropertyValue.get(property, serializedValue));
  }

  public void addChildren(MetaPointer containment, List<String> childrenIds) {
    initContainments();
    this.containments.add(new SerializedContainmentValue(containment, childrenIds));
  }

  /**
   * Removes the specified childId from the containments, if present.
   *
   * @param childId the identifier of the childId to be removed; must not be null
   * @return true if the childId was successfully removed, false otherwise
   */
  public boolean removeChild(@Nonnull String childId) {
    Objects.requireNonNull(childId, "childId should not be null");
    if (this.containments == null) {
      return false;
    }
    for (SerializedContainmentValue containment : this.containments) {
      if (containment.removeChild(childId)) {
        return true;
      }
    }
    return false;
  }

  public void addReferenceValue(
      MetaPointer reference, List<SerializedReferenceValue.Entry> referenceValues) {
    if (this.references == null) {
      this.references = new ArrayList<>(1);
    }
    this.references.add(new SerializedReferenceValue(reference, referenceValues));
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

  @Nonnull
  public List<String> getContainmentValues(@Nonnull MetaPointer containmentMetaPointer) {
    for (SerializedContainmentValue cv : this.getContainments()) {
      if (containmentMetaPointer.equals(cv.getMetaPointer())) {
        return Collections.unmodifiableList(cv.getChildrenIds());
      }
    }
    return Collections.emptyList();
  }

  public void setAnnotations(List<String> annotationIDs) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedClassifierInstance)) return false;
    SerializedClassifierInstance that = (SerializedClassifierInstance) o;
    return Objects.equals(id, that.id)
        && Objects.equals(classifier, that.classifier)
        && Objects.equals(parentNodeID, that.parentNodeID)
        && Objects.equals(properties, that.properties)
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
        properties,
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
        + properties
        + ", containments="
        + getContainments()
        + ", references="
        + getReferences()
        + ", annotations="
        + getAnnotations()
        + '}';
  }

  private void initReferences() {
    if (this.references == null) {
      this.references = new ArrayList<>(1);
    }
  }

  private void initContainments() {
    if (this.containments == null) {
      this.containments = new ArrayList<>();
    }
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
      for (SerializedContainmentValue containmentValue : this.containments) {
        for (String childId : containmentValue.getChildrenIds()) {
          if (Objects.equals(childId, id)) {
            return true;
          }
        }
      }
    }
    if (this.annotations != null) {
      return this.annotations.contains(id);
    }
    return false;
  }
}
