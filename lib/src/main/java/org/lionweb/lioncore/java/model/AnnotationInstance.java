package org.lionweb.lioncore.java.model;

import org.lionweb.lioncore.java.metamodel.Annotation;

/**
 * While an AnnotationInstance implements HasFeatureValues, it is forbidden to hold any Containment links.
 */
public interface AnnotationInstance extends HasFeatureValues {
    Annotation getAnnotationDefinition();
}
