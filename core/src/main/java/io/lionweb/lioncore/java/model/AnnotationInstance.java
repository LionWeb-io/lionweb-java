package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Annotation;
import io.lionweb.lioncore.java.versions.LionWebVersionToken;

/**
 * While an AnnotationInstance implements HasFeatureValues, it is forbidden to hold any children, as
 * the Annotation should not have any containment link.
 */
public interface AnnotationInstance<V extends LionWebVersionToken> extends ClassifierInstance<Annotation<V>, V> {
  Annotation getAnnotationDefinition();

  default Annotation getClassifier() {
    return getAnnotationDefinition();
  }
}
