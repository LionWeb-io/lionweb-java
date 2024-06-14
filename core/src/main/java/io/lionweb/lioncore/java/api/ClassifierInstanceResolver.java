package io.lionweb.lioncore.java.api;

import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** This is able to find a Node or an Annotation Instance given its ID. */
public interface ClassifierInstanceResolver {

  /**
   * This returns the Classifier Instance or null if the Classifier Instance cannot be found by this
   * Classifier InstanceResolver.
   */
  @Nullable
  ClassifierInstance<?> resolve(String instanceID);

  default boolean canResolve(String instanceID) {
    return resolve(instanceID) != null;
  }

  @Nonnull
  default ClassifierInstance<?> strictlyResolve(String instanceID) {
    ClassifierInstance<?> partial = resolve(instanceID);
    if (partial == null) {
      throw new UnresolvedClassifierInstanceException(instanceID);
    } else {
      return partial;
    }
  }

  @Nonnull
  default ClassifierInstance<?> resolveOrProxy(String instanceID) {
    ClassifierInstance<?> partial = resolve(instanceID);
    return partial == null ? new ProxyNode(instanceID) : partial;
  }
}
