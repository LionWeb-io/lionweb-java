package io.lionweb.api;

import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.impl.ProxyNode;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** This is able to find a Node or an Annotation Instance given its ID. */
public interface ClassifierInstanceResolver {

  /**
   * This returns the Classifier Instance or null if the Classifier Instance cannot be found by this
   * Classifier InstanceResolver.
   *
   * <p>If instanceID is null, this method should return null.
   */
  @Nullable
  ClassifierInstance<?> resolve(@Nullable String instanceID);

  /**
   * Determines whether a classifier instance with the given ID can be resolved.
   *
   * @param instanceID the ID of the classifier instance to be resolved
   * @return {@code true} if the classifier instance can be resolved, {@code false} otherwise
   */
  default boolean canResolve(@Nonnull String instanceID) {
    return resolve(instanceID) != null;
  }

  /**
   * Resolves the classifier instance associated with the given ID. This method ensures that a valid
   * {@link ClassifierInstance} is always returned. If the instance cannot be resolved (i.e., it
   * does not exist), an {@link UnresolvedClassifierInstanceException} is thrown.
   *
   * @param instanceID the identifier of the classifier instance to resolve, must not be null
   * @return the {@link ClassifierInstance} associated with the given ID
   * @throws UnresolvedClassifierInstanceException if no classifier instance can be resolved for the
   *     provided ID
   */
  @Nonnull
  default ClassifierInstance<?> strictlyResolve(@Nonnull String instanceID) {
    ClassifierInstance<?> partial = resolve(instanceID);
    if (partial == null) {
      throw new UnresolvedClassifierInstanceException(instanceID);
    } else {
      return partial;
    }
  }

  /**
   * Resolves the classifier instance associated with the given ID. If the instance cannot be
   * resolved, a proxy representing the unresolved state is returned instead.
   *
   * @param instanceID the identifier of the classifier instance to resolve, must not be null
   * @return the resolved {@link ClassifierInstance} if found, or a proxy instance of {@link
   *     ClassifierInstance} if the specified ID cannot be resolved
   */
  @Nonnull
  default ClassifierInstance<?> resolveOrProxy(@Nonnull String instanceID) {
    ClassifierInstance<?> partial = resolve(instanceID);
    return partial == null ? new ProxyNode(instanceID) : partial;
  }
}
