package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.language.Annotation;
import io.lionweb.lioncore.java.language.Classifier;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

public interface ClassifierInstance<T extends Classifier<T>> extends HasFeatureValues {
  /** Return all the annotations associated to this ClassifierInstance. */
  @Nonnull
  List<AnnotationInstance> getAnnotations();

  /**
   * Given a specific Annotation type it returns either the list of instances of that Annotation
   * associated to the Node.
   */
  @Nonnull
  List<AnnotationInstance> getAnnotations(@Nonnull Annotation annotation);

  /**
   * If an annotation instance was already associated under the Annotation link used by this
   * AnnotationInstance, and the annotation does not support multiple values, then the existing
   * instance will be removed and replaced by the instance specified in the call to this method.
   *
   * @throws IllegalArgumentException In case the specified Annotation link cannot be used on Nodes
   *     of this Concept.
   */
  void addAnnotation(@Nonnull AnnotationInstance instance);

  void removeAnnotation(@Nonnull AnnotationInstance instance);

  String getID();

  T getClassifier();

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
    for (Node child : ClassifierInstanceUtils.getChildren(self)) {
      if (!(child instanceof ProxyNode)) {
        collectSelfAndDescendants((T) child, includeAnnotations, result);
      }
    }
  }
}
