package org.lionweb.lioncore.java;

import java.util.List;

/**
 * Something which can own Features.
 *
 * For example, a Concept can have several features.
 *
 * In Ecore there is no equivalent as only EClasses can have features, while in LionCore, also Annotations can.
 */
public interface FeaturesContainer {
    List<Feature> getFeatures();
}
