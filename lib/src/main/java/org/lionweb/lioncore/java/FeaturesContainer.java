package org.lionweb.lioncore.java;

import java.util.List;

/**
 * Something which can own {@link Feature}s.
 *
 * For example, a {@link Concept} can have several features.
 *
 * In Ecore there is no equivalent as only EClasses can have features, while in LionCore, also {@link Annotation}s can.
 */
public interface FeaturesContainer {
    List<Feature> getFeatures();
}
