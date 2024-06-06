package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Annotation;

/**
 * While an AnnotationInstance implements HasFeatureValues, it is forbidden to hold any children, as
 * the Annotation should not have any containment link.
 */
public interface AnnotationInstance extends ClassifierInstance<Annotation> {
  Annotation getAnnotationDefinition();

  default Annotation getClassifier() {
    return getAnnotationDefinition();
  }
}
