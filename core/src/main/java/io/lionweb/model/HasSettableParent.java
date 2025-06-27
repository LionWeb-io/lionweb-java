package io.lionweb.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Certain nodes have a parent that can be set. This is typically done for consistency when adding a
 * node into a containment.
 */
public interface HasSettableParent {
  /**
   * Set a new parent.
   *
   * @param parent the new parent to be assigned
   * @return the element itself
   */
  @Nonnull
  ClassifierInstance<?> setParent(@Nullable ClassifierInstance<?> parent);
}
