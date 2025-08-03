package io.lionweb.client.delta;

import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;

/**
 * We refer to references by the tuple of (parent, reference, index). Rationale: Both reference
 * target and resolveInfo can be null, so they aren’t suitable for identifying the reference.
 * However, target and resolveInfo MUST NOT both be null.
 */
public class ReferenceIdentifier {
  public final @NotNull String parentId;
  public final @NotNull MetaPointer reference;
  public final int index;

  public ReferenceIdentifier(@NotNull String parentId, @NotNull MetaPointer reference, int index) {
    this.parentId = parentId;
    this.reference = reference;
    this.index = index;
  }
}
