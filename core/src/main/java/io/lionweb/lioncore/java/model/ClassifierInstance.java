package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Classifier;
import java.util.List;

public interface ClassifierInstance<T extends Classifier<T>> extends HasFeatureValues {
  List<AnnotationInstance> getAnnotations();

  String getID();

  Classifier<T> getClassifier();

  /** The immediate parent of the Node. This should be null only for root nodes. */
  ClassifierInstance<T> getParent();
}
