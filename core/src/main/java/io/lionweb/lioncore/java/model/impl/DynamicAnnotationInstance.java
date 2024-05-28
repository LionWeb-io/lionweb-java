package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.Annotation;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import java.util.Objects;

public class DynamicAnnotationInstance extends DynamicClassifierInstance<Annotation>
    implements AnnotationInstance {

  private Annotation annotation;
  private ClassifierInstance<?> annotated;

  public DynamicAnnotationInstance(String id) {
    this.id = id;
  }

  public DynamicAnnotationInstance(String id, Annotation annotation) {
    this(id);
    this.annotation = annotation;
  }

  public DynamicAnnotationInstance(
      String id, Annotation annotation, ClassifierInstance<?> annotated) {
    this(id, annotation);
    setAnnotated(annotated);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DynamicAnnotationInstance)) {
      return false;
    }
    DynamicAnnotationInstance that = (DynamicAnnotationInstance) o;
    return Objects.equals(annotation, that.annotation)
        && Objects.equals(id, that.id)
        && Objects.equals(annotated, that.annotated)
        && Objects.equals(propertyValues, that.propertyValues)
        && Objects.equals(containmentValues, that.containmentValues)
        && Objects.equals(referenceValues, that.referenceValues)
        && Objects.equals(annotations, that.annotations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, annotation, annotated);
  }

  public void setAnnotation(Annotation annotation) {
    this.annotation = annotation;
  }

  public void setAnnotated(ClassifierInstance<?> annotated) {
    if (annotated == this.annotated) {
      // necessary to avoid infinite loops
      return;
    }
    if (this.annotated != null && this.annotated instanceof DynamicNode) {
      ((DynamicNode) this.annotated).tryToRemoveAnnotation(this);
    }
    this.annotated = annotated;
    if (this.annotated != null && this.annotated instanceof AbstractClassifierInstance) {
      ((AbstractClassifierInstance<?>) this.annotated).addAnnotation(this);
    }
  }

  @Override
  public Annotation getAnnotationDefinition() {
    return annotation;
  }

  @Override
  public ClassifierInstance getParent() {
    return annotated;
  }

  @Override
  public String toString() {
    String annotatedDesc = null;
    if (annotated != null) {
      annotatedDesc = annotated.getID();
    }
    return "DynamicAnnotationInstance{"
        + "annotation="
        + annotation
        + ", annotated="
        + annotatedDesc
        + '}';
  }
}
