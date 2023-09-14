package io.lionweb.lioncore.java.serialization.data;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SerializedAnnotationInstance extends SerializedClassifierInstance {
  private String annotatedID;

  public String getAnnotated() {
    return annotatedID;
  }

  public void setAnnotated(String annotatedID) {
    this.annotatedID = annotatedID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedAnnotationInstance)) return false;
    SerializedAnnotationInstance that = (SerializedAnnotationInstance) o;
    return Objects.equals(id, that.id)
            && Objects.equals(classifier, that.classifier)
            && Objects.equals(annotatedID, that.annotatedID)
            && Objects.equals(properties, that.properties)
            && Objects.equals(containments, that.containments)
            && Objects.equals(annotations, that.annotations)
            && Objects.equals(references, that.references);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, classifier, annotatedID, properties, containments, references);
  }

  @Override
  public String toString() {
    return "SerializedAnnotationInstance{"
            + "id='"
            + id
            + '\''
            + ", concept="
            + classifier
            + ", annotatedID='"
            + annotatedID
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
