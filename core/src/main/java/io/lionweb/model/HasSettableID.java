package io.lionweb.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Certain nodes have an ID that can be set. */
public interface HasSettableID {
  /**
   * Set a new ID.
   *
   * @param id the new UD to be assigned
   * @return the element itself
   */
  @Nonnull
  ClassifierInstance<?> setID(@Nullable String id);
}
