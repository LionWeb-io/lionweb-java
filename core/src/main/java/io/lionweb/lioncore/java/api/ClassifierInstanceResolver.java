package io.lionweb.lioncore.java.api;

import io.lionweb.lioncore.java.model.ClassifierInstance;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** This is able to find a Node given its ID. */
public interface ClassifierInstanceResolver {

  /** This returns the Node or null if the Node cannot be found by this NodeResolver. */
  @Nullable
  ClassifierInstance<?> resolve(String nodeID);

  @Nonnull
  default ClassifierInstance<?> strictlyResolve(String nodeID) {
    ClassifierInstance<?> partial = resolve(nodeID);
    if (partial == null) {
      throw new UnresolvedClassifierInstanceException(nodeID);
    } else {
      return partial;
    }
  }
}
