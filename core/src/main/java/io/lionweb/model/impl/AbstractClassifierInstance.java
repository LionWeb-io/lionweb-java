package io.lionweb.model.impl;

import io.lionweb.language.Annotation;
import io.lionweb.language.Classifier;
import io.lionweb.language.Containment;
import io.lionweb.language.Reference;
import io.lionweb.model.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractClassifierInstance<T extends Classifier<T>>
    implements ClassifierInstance<T> {
  /**
   * Most nodes will have no annotations, so when holding millions of nodes in memory it is
   * convenient to avoid unnecessary allocations. Based on this, this field will be null when no
   * annotations are present, so that the memory footprint can be contained.
   */
  @Nullable protected List<AnnotationInstance> annotations = null;

  // Public methods for annotations

  @Override
  public @Nonnull List<AnnotationInstance> getAnnotations() {
    if (annotations == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(annotations);
  }

  /**
   * Given a specific Annotation type it returns either the list of instances of that Annotation
   * associated to the Node.
   */
  @Override
  public @Nonnull List<AnnotationInstance> getAnnotations(@Nonnull Annotation annotation) {
    if (annotations == null) {
      return Collections.emptyList();
    }
    return annotations.stream()
        .filter(a -> a.getAnnotationDefinition() == annotation)
        .collect(Collectors.toList());
  }

  @Override
  public boolean addAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (this.annotations == null) {
      this.annotations = new ArrayList<>();
    }
    if (this.annotations.contains(instance)) {
      // necessary to avoid infinite loops and duplicate insertions
      return false;
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(this);
    }
    if (this.annotations.contains(instance)) {
      // necessary to avoid infinite loops and duplicate insertions
      // the previous setAnnotated could potentially have already set annotations
      return false;
    }
    this.annotations.add(instance);
    return true;
  }

  @Override
  public int removeAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    int index = -1;
    if (annotations != null) {
      index = this.annotations.indexOf(instance);
    }
    if (index == -1) {
      throw new IllegalArgumentException();
    }
    annotations.remove(index);
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(null);
    }
    return index;
  }

  void tryToRemoveAnnotation(@Nonnull AnnotationInstance instance) {
    Objects.requireNonNull(instance);
    if (annotations == null || !this.annotations.remove(instance)) {
      return;
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(null);
    }
  }

  // Public methods for containments

  @Override
  public void removeChild(Node child) {
    for (Containment containment : this.getClassifier().allContainments()) {
      List<? extends Node> children = this.getChildren(containment);
      if (children.remove(child)) {
        if (child instanceof HasSettableParent) {
          ((HasSettableParent) child).setParent(null);
        }
        return;
      }
    }
  }

  @Override
  public void removeChild(@Nonnull Containment containment, int index) {
    if (!getClassifier().allContainments().contains(containment)) {
      throw new IllegalArgumentException("Containment not belonging to this concept");
    }
    List<? extends Node> children = this.getChildren(containment);
    if (children.size() > index) {
      children.remove(index);
    } else {
      throw new IllegalArgumentException(
          "Invalid index " + index + " when children are " + children.size());
    }
  }

  // Public methods for references

  @Override
  public void removeReferenceValue(@Nonnull Reference reference, int index) {
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept");
    }
    ReferenceValue removedReferenceValue = getReferenceValues(reference).remove(index);
    if (observer != null) {
      observer.referenceValueRemoved(this, reference, index, removedReferenceValue);
    }
  }

  @Override
  public void removeReferenceValue(
      @Nonnull Reference reference, @Nullable ReferenceValue referenceValue) {
    if (!getClassifier().allReferences().contains(reference)) {
      throw new IllegalArgumentException("Reference not belonging to this concept");
    }
    if (!getReferenceValues(reference).remove(referenceValue)) {
      throw new IllegalArgumentException(
          "The given reference value could not be found under reference " + reference.getName());
    }
  }

  // Observer methods

  @Override
  public void addObserver(@Nullable ClassifierInstanceObserver observer) {
    this.observer = observer;
  }

  @Override
  public void removeObserver(@Nonnull ClassifierInstanceObserver observer) {
    throw new UnsupportedOperationException();
  }

  /**
   * In most cases we will have no observers or one observer, shared across many nodes, so we avoid
   * instantiating lists. We Represent multiple observers with a CompositeObserver instead.
   */
  protected @Nullable ClassifierInstanceObserver observer = null;
}
