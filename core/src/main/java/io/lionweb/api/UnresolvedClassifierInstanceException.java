package io.lionweb.api;

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Exception indicating that a classifier instance could not be resolved using the provided
 * identifier.
 *
 * <p>This runtime exception is typically thrown by implementations of the {@code
 * ClassifierInstanceResolver} interface when an attempt to strictly resolve a classifier instance
 * fails due to the absence of an instance corresponding to the specified ID.
 */
public class UnresolvedClassifierInstanceException extends RuntimeException {
  private @Nonnull String instanceID;

  /** Retrieves the instance ID of the classifier instance that could not be resolved. */
  public @Nonnull String getInstanceID() {
    return instanceID;
  }

  /**
   * Constructs a new {@code UnresolvedClassifierInstanceException} with the specified instance ID.
   * This exception is thrown when a classifier instance cannot be resolved using the provided ID.
   *
   * @param instanceID the unique identifier of the classifier instance that could not be resolved.
   *     Must not be null.
   */
  public UnresolvedClassifierInstanceException(@Nonnull String instanceID) {
    super("Unable to resolve classifier instance with ID=" + instanceID);
    Objects.requireNonNull(instanceID, "instanceID should not be null");
    this.instanceID = instanceID;
  }
}
