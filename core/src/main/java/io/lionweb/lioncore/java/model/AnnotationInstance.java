package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Annotation;

/**
 * While an AnnotationInstance implements HasFeatureValues, it is forbidden to hold any Containment
 * links.
 */
public interface AnnotationInstance extends Element {
  Annotation getAnnotationDefinition();

  Element getAnnotated();
}
