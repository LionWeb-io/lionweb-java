package io.lionweb.model.impl;

import io.lionweb.language.Concept;
import io.lionweb.model.*;
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
      if (this.observer == null) {
        thisAndAllDescendants().forEach(ClassifierInstance::partitionObserverUnregistered);
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
}
