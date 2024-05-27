package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Classifier;
import java.util.List;

public interface ClassifierInstance<T extends Classifier<T>> extends HasFeatureValues {
  /** Return all the annotations associated to this Node. */
  List<AnnotationInstance> getAnnotations();

  String getID();

  Classifier<T> getClassifier();

  /** The immediate parent of the Node. This should be null only for root nodes. */
  ClassifierInstance<T> getParent();
}
