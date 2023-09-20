package io.lionweb.lioncore.java.serialization.data;

import java.util.Objects;

public class SerializedAnnotationInstance extends SerializedClassifierInstance {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedAnnotationInstance)) return false;
    SerializedAnnotationInstance that = (SerializedAnnotationInstance) o;
    return Objects.equals(id, that.id)
        && Objects.equals(classifier, that.classifier)
        && Objects.equals(parentNodeID, that.parentNodeID)
        && Objects.equals(properties, that.properties)
        && Objects.equals(containments, that.containments)
        && Objects.equals(annotations, that.annotations)
        && Objects.equals(references, that.references);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, classifier, parentNodeID, properties, containments, references);
  }

  @Override
  public String toString() {
    return "SerializedAnnotationInstance{"
        + "id='"
        + id
        + '\''
        + ", concept="
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
