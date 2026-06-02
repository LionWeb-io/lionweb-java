package io.lionweb.model;

import io.lionweb.language.Annotation;
import javax.annotation.Nonnull;

/**
 * While an AnnotationInstance implements HasFeatureValues, it is forbidden to hold any children, as
 * the Annotation should not have any containment link.
 */
public interface AnnotationInstance extends ClassifierInstance<Annotation> {
  @Nonnull
  Annotation getAnnotationDefinition();

  default @Nonnull Annotation getClassifier() {
    return getAnnotationDefinition();
  }
}
