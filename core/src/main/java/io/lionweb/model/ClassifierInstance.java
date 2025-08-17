package io.lionweb.model;

import io.lionweb.language.Annotation;
import io.lionweb.language.Classifier;
import io.lionweb.model.impl.ProxyNode;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   * Add the annotation. Note that we can always have multiple annotation instances of the same
   * annotation type attached to a node, so this will never replace an existing annotation instance.
   *
   * @throws IllegalArgumentException In case the specified Annotation link cannot be used on Nodes
   *     of this Concept.
   * @return true if the annotation has been actually added, false if it was already present
   */
  boolean addAnnotation(@Nonnull AnnotationInstance instance);

  /**
   * Remove the given annotation from this classifier instance.
   *
   * @param instance annotation to be removed
   * @return index of the removed annotation
   * @throws IllegalArgumentException if the annotation is not present
   */
  int removeAnnotation(@Nonnull AnnotationInstance instance);

  String getID();

  T getClassifier();

  /** The immediate parent of the Node. This should be null only for root nodes. */
  @Nullable
  ClassifierInstance<?> getParent();

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

  // Observer methods

  void registerObserver(@Nonnull ClassifierInstanceObserver observer);

  void unregisterObserver(@Nonnull ClassifierInstanceObserver observer);
}
