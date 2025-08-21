package io.lionweb.model.impl;

import io.lionweb.language.Concept;
import io.lionweb.language.Reference;
import io.lionweb.model.*;
import java.util.IdentityHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractNode extends AbstractClassifierInstance<Concept> implements Node {
  @Override
  public void registerPartitionObserver(@Nullable PartitionObserver observer) {
    if (!this.isRoot()) {
      throw new UnsupportedOperationException(
          "Cannot register a partition observer on a node which is not root");
    }
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
      this.observer = CompositePartitionObserver.combine(this.observer, observer);
    }
    thisAndAllDescendants().forEach(d -> d.partitionObserverRegistered(this.observer));
  }

  @Override
  public void unregisterPartitionObserver(@Nonnull PartitionObserver observer) {
    if (!this.isRoot()) {
      throw new UnsupportedOperationException(
          "Cannot unregister a partition observer on a node which is not root");
    }
    if (this.observer == observer) {
      this.observer = null;
      thisAndAllDescendants().forEach(ClassifierInstance::partitionObserverUnregistered);
      return;
    }
    if (this.observer instanceof CompositePartitionObserver) {
      this.observer = ((CompositePartitionObserver) this.observer).remove(observer);
      thisAndAllDescendants().forEach(ClassifierInstance::partitionObserverUnregistered);
      if (this.observer == null) {
        refObservers.forEach(ReferenceValue::unregisterObserver);
        refObservers = null;
      } else {
        thisAndAllDescendants().forEach(d -> d.partitionObserverRegistered(this.observer));
      }
    } else {
      throw new IllegalArgumentException("Observer not registered: " + observer);
    }
  }

  /**
   * In most cases we will have no observers or one observer, shared across many nodes, so we avoid
   * instantiating lists. We Represent multiple observers with a CompositeObserver instead.
   */
  protected @Nullable PartitionObserver observer = null;

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
