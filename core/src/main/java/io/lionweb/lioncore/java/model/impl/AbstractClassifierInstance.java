package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.Annotation;
import io.lionweb.lioncore.java.language.Classifier;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractClassifierInstance<T extends Classifier<T>>
    implements ClassifierInstance<T> {
  protected final List<AnnotationInstance> annotations = new ArrayList<>();

  // Public methods for annotations

  @Override
  public List<AnnotationInstance> getAnnotations() {
    return Collections.unmodifiableList(annotations);
  }

  /**
   * Given a specific Annotation type it returns either the list of instances of that Annotation
   * associated to the Node.
   */
  @Nonnull
  public List<AnnotationInstance> getAnnotations(@Nonnull Annotation annotation) {
    return annotations.stream()
        .filter(a -> a.getAnnotationDefinition() == annotation)
        .collect(Collectors.toList());
  }

  /**
   * If an annotation instance was already associated under the Annotation link used by this
   * AnnotationInstance, and the annotation does not support multiple values, then the existing
   * instance will be removed and replaced by the instance specified in the call to this method.
   *
   * @throws IllegalArgumentException In case the specified Annotation link cannot be used on Nodes
   *     of this Concept.
   */
  public void addAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (this.annotations.contains(instance)) {
      // necessary to avoid infinite loops and duplicate insertions
      return;
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(this);
    }
    if (this.annotations.contains(instance)) {
      // necessary to avoid infinite loops and duplicate insertions
      // the previous setAnnotated could potentially have already set annotations
      return;
    }
    this.annotations.add(instance);
  }

  public void removeAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (!this.annotations.remove(instance)) {
      throw new IllegalArgumentException();
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(null);
    }
  }

  void tryToRemoveAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (!this.annotations.remove(instance)) {
      return;
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(null);
    }
  }

  // Public methods for containments

  @Override
  @Nonnull
  public List<Node> getChildren() {
    List<Node> allChildren = new LinkedList<>();
    getClassifier().allContainments().stream()
        .map(c -> getChildren(c))
        .forEach(children -> allChildren.addAll(children));
    return allChildren;
  }

  // Public methods for references

  @Nonnull
  @Override
  public List<ReferenceValue> getReferenceValues() {
    List<ReferenceValue> allReferredValues = new LinkedList<>();
    getClassifier().allReferences().stream()
        .map(r -> getReferenceValues(r))
        .forEach(referenceValues -> allReferredValues.addAll(referenceValues));
    return allReferredValues;
  }

  @Nonnull
  @Override
  public List<Node> getReferredNodes() {
    return getReferenceValues().stream()
        .map(rv -> rv.getReferred())
        .filter(n -> n != null)
        .collect(Collectors.toList());
  }

  @Override
  public void setOnlyReferenceValueByName(String referenceName, @Nullable ReferenceValue value) {}

  @Override
  public void setReferenceValuesByName(
      String referenceName, @Nonnull List<ReferenceValue> values) {}
}
