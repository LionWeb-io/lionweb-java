package io.lionweb.lioncore.java.model;

import javax.annotation.Nullable;

/**
 * Certain nodes have a parent that can be set. This is typically done for consistency when adding a
 * node into a containment.
 */
public interface HasSettableParent {
  void setParent(@Nullable Node parent);
}
