package io.lionweb.lioncore.java.serialization.data;

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
  private final List<SerializedContainmentValue> containments = new ArrayList<>();
  private final List<SerializedReferenceValue> references = new ArrayList<>();
  private final List<String> annotations = new ArrayList<>();
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

  public List<SerializedContainmentValue> getContainments() {
    return Collections.unmodifiableList(this.containments);
  }

  public List<String> getChildren() {
    List<String> children = new ArrayList<>();
    this.containments.forEach(c -> children.addAll(c.getValue()));
    return Collections.unmodifiableList(children);
  }

  public List<SerializedReferenceValue> getReferences() {
    return Collections.unmodifiableList(this.references);
  }

  public List<String> getAnnotations() {
    return Collections.unmodifiableList(this.annotations);
  }

  public List<SerializedPropertyValue> getProperties() {
    return Collections.unmodifiableList(properties);
  }

  public void addPropertyValue(SerializedPropertyValue propertyValue) {
    this.properties.add(propertyValue);
  }

  public void addContainmentValue(SerializedContainmentValue containmentValue) {
    this.containments.add(containmentValue);
  }

  public void addReferenceValue(SerializedReferenceValue referenceValue) {
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
    this.containments.add(new SerializedContainmentValue(containment, childrenIds));
  }

  public void addReferenceValue(
      MetaPointer reference, List<SerializedReferenceValue.Entry> referenceValues) {
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
    this.annotations.clear();
    this.annotations.addAll(annotationIDs);
  }

  public void addAnnotation(String annotationID) {
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
        && Objects.equals(containments, that.containments)
        && Objects.equals(references, that.references)
        && Objects.equals(annotations, that.annotations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, classifier, parentNodeID, properties, containments, references, annotations);
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
        + containments
        + ", references="
        + references
        + ", annotations="
        + annotations
        + '}';
  }
}
