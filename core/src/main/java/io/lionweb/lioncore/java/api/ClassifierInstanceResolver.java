package io.lionweb.lioncore.java.api;

import io.lionweb.lioncore.java.model.ClassifierInstance;
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

  @Nonnull
  default ClassifierInstance<?> strictlyResolve(String instanceID) {
    ClassifierInstance<?> partial = resolve(instanceID);
    if (partial == null) {
      throw new UnresolvedClassifierInstanceException(instanceID);
    } else {
      return partial;
    }
  }
}
