package io.lionweb.serialization.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    this.containments.forEach(c -> children.addAll(c.getValue()));
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

  public void addPropertyValue(SerializedPropertyValue propertyValue) {
    this.properties.add(propertyValue);
  }

  public void addContainmentValue(SerializedContainmentValue containmentValue) {
    initContainments();
    this.containments.add(containmentValue);
  }

  public void addReferenceValue(SerializedReferenceValue referenceValue) {
    initReferences();
    this.references.add(referenceValue);
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
    this.properties.add(new SerializedPropertyValue(property, serializedValue));
  }

  public void addChildren(MetaPointer containment, List<String> childrenIds) {
    initContainments();
    this.containments.add(new SerializedContainmentValue(containment, childrenIds));
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
        return Collections.unmodifiableList(cv.getValue());
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
}
