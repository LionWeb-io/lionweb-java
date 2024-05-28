package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Classifier;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import java.util.Collection;
import java.util.List;

public interface ClassifierInstance<T extends Classifier<T>> extends HasFeatureValues {
  /** Return all the annotations associated to this ClassifierInstance. */
  List<AnnotationInstance> getAnnotations();

  String getID();

  Classifier<T> getClassifier();

  /** The immediate parent of the Node. This should be null only for root nodes. */
  ClassifierInstance<T> getParent();

  /** Collects `self` and all its descendants into `result`. */
  static <T extends ClassifierInstance<?>> void collectSelfAndDescendants(
      T self, boolean includeAnnotations, Collection<T> result) {
    result.add(self);
    if (includeAnnotations) {
      for (AnnotationInstance annotation : self.getAnnotations()) {
        collectSelfAndDescendants((T) annotation, includeAnnotations, result);
      }
    }
    for (Node child : self.getChildren()) {
      if (!(child instanceof ProxyNode)) {
        collectSelfAndDescendants((T) child, includeAnnotations, result);
      }
    }
  }
}
