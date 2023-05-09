package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.Experimental;
import io.lionweb.lioncore.java.language.Annotation;

/**
 * While an AnnotationInstance implements HasFeatureValues, it is forbidden to hold any Containment
 * links.
 */
@Experimental
public interface AnnotationInstance extends HasFeatureValues {
  Annotation getAnnotationDefinition();
}
