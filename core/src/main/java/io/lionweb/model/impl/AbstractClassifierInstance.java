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
    if (instance.getID() != null
        && annotations.stream().anyMatch(a -> a.getID().equals(instance.getID()))) {
      // necessary to avoid infinite loops and duplicate insertions
      return false;
    }
    if (instance instanceof DynamicAnnotationInstance) {
      ((DynamicAnnotationInstance) instance).setAnnotated(this);
    }
    if (instance.getID() == null
        || annotations.stream().noneMatch(a -> a.getID().equals(instance.getID()))) {
      this.annotations.add(instance);
      if (observer != null) {
        observer.annotationAdded(this, this.annotations.size() - 1, instance);
      }
    }
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
    if (observer != null) {
      observer.annotationRemoved(this, index, instance);
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
  public void registerObserver(@Nullable ClassifierInstanceObserver observer) {
    if (this.observer == observer) {
      throw new IllegalArgumentException("Observer already registered: " + observer);
    }
    if (this.observer == null) {
      if (refObservers == null) {
        refObservers = new IdentityHashMap<>();
      }
      // We track the ObserverOnReferenceValue, so that we can remove them later,
      // if observer is set to null
      getClassifier()
          .allReferences()
          .forEach(
              reference -> {
                for (int i = 0; i < this.getReferenceValues(reference).size(); i++) {
                  ReferenceValue referenceValue = this.getReferenceValues(reference).get(i);
                  ObserverOnReferenceValue newRefObserver =
                      new ObserverOnReferenceValue(this, reference, i);
                  referenceValue.registerObserver(newRefObserver);
                  refObservers.put(referenceValue, newRefObserver);
                }
              });
      this.observer = observer;
    } else {
      this.observer = CompositeClassifierInstanceObserver.combine(this.observer, observer);
    }
  }

  @Override
  public void unregisterObserver(@Nonnull ClassifierInstanceObserver observer) {
    if (this.observer == observer) {
      this.observer = null;
      return;
    }
    if (this.observer instanceof CompositeClassifierInstanceObserver) {
      this.observer = ((CompositeClassifierInstanceObserver) this.observer).remove(observer);
      if (this.observer == null) {
        refObservers.forEach(ReferenceValue::unregisterObserver);
        refObservers = null;
      }
    } else {
      throw new IllegalArgumentException("Observer not registered: " + observer);
    }
  }

  /**
   * In most cases we will have no observers or one observer, shared across many nodes, so we avoid
   * instantiating lists. We Represent multiple observers with a CompositeObserver instead.
   */
  protected @Nullable ClassifierInstanceObserver observer = null;

  protected class ObserverOnReferenceValue implements ReferenceValueObserver {

    private ClassifierInstance<?> classifierInstance;
    private Reference reference;
    private int index;

    public ObserverOnReferenceValue(
        ClassifierInstance<?> classifierInstance, Reference reference, int index) {
      this.classifierInstance = classifierInstance;
      this.reference = reference;
      this.index = index;
    }

    @Override
    public void resolveInfoChanged(
        @Nonnull ReferenceValue referenceValue,
        @Nullable String oldValue,
        @Nullable String newValue) {
      if (observer != null) {
        observer.referenceValueChanged(
            classifierInstance,
            reference,
            index,
            referenceValue.getReferredID(),
            oldValue,
            referenceValue.getReferredID(),
            newValue);
      }
    }

    @Override
    public void referredIDChanged(
        @Nonnull ReferenceValue referenceValue,
        @Nullable String oldValue,
        @Nullable String newValue) {
      if (observer != null) {
        observer.referenceValueChanged(
            classifierInstance,
            reference,
            index,
            oldValue,
            referenceValue.getResolveInfo(),
            newValue,
            referenceValue.getResolveInfo());
      }
    }
  }

  private IdentityHashMap<ReferenceValue, ObserverOnReferenceValue> refObservers = null;
}
