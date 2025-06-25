package io.lionweb.lioncore.java.model;

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
  ClassifierInstance<?> setParent(@Nullable ClassifierInstance<?> parent);
}
