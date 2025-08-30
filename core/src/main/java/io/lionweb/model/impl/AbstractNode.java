package io.lionweb.model.impl;

import io.lionweb.language.Concept;
import io.lionweb.model.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractNode extends AbstractClassifierInstance<Concept> implements Node {

  /**
   * Determines whether a given {@link PartitionObserver} is the other or contained within the
   * other. This includes checking if the two observers are the same instance or if the target
   * observer is part of a {@link CompositePartitionObserver}.
   *
   * @param current the current {@link PartitionObserver}, or null.
   * @param observer the {@link PartitionObserver} to search for, or null.
   * @return {@code true} if the observer is contained within the current observer, {@code false}
   *     otherwise.
   */
  static boolean contains(
      @Nullable PartitionObserver current, @Nullable PartitionObserver observer) {
    if (current == null || observer == null) {
      return false;
    }
    if (current == observer) {
      return true;
    }
    if (current instanceof CompositePartitionObserver) {
      return ((CompositePartitionObserver) current)
          .getElements().stream().anyMatch(e -> contains(e, observer));
    }
    return false;
  }

  @Override
  public boolean registerPartitionObserver(@Nullable PartitionObserver observer) {
    if (!this.isRoot()) {
      throw new UnsupportedOperationException(
          "Cannot register a partition observer on a node which is not root");
    }
    if (contains(this.observer, observer)) {
      return false;
    }
    if (this.observer == null) {
      this.observer = observer;
    } else {
      this.observer = CompositePartitionObserver.combine(this.observer, observer);
    }
    this.partitionObserverRegistered(this.observer);
    return true;
  }

  @Override
  public void unregisterPartitionObserver(@Nonnull PartitionObserver observer) {
    if (!this.isRoot()) {
      throw new UnsupportedOperationException(
          "Cannot unregister a partition observer on a node which is not root");
    }
    if (this.observer == observer) {
      this.observer = null;
      this.partitionObserverRegistered(this.observer);
      return;
    }
    if (this.observer instanceof CompositePartitionObserver) {
      this.observer = ((CompositePartitionObserver) this.observer).remove(observer);
    } else {
      throw new IllegalArgumentException("Observer not registered: " + observer);
    }
    this.partitionObserverRegistered(this.observer);
  }

  /**
   * In most cases we will have no observers or one observer, shared across many nodes, so we avoid
   * instantiating lists. We Represent multiple observers with a CompositeObserver instead.
   */
  protected @Nullable PartitionObserver observer = null;
}
