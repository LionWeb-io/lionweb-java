package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Replace existing entry oldTarget/oldResolveInfo inside parent's reference at index with
 * newTarget/newResolveInfo.
 */
public final class ChangeReference extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;

  /** Old target node id. */
  public final @Nullable String oldReference;

  public final @Nullable String oldResolveInfo;

  /** New target node id. */
  public final @Nullable String newReference;

  public final @Nullable String newResolveInfo;

  public ChangeReference(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String oldReference,
      @Nullable String oldResolveInfo,
      @Nullable String newReference,
      @Nullable String newResolveInfo) {
    super(commandId);
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(reference, "reference must not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index must be non-negative");
    }
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.oldReference = oldReference;
    this.oldResolveInfo = oldResolveInfo;
    this.newReference = newReference;
    this.newResolveInfo = newResolveInfo;
  }

  @Override
  public String toString() {
    return "ChangeReference{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", oldReference='"
        + oldReference
        + '\''
        + ", oldResolveInfo='"
        + oldResolveInfo
        + '\''
        + ", newReference='"
        + newReference
        + '\''
        + ", newResolveInfo='"
        + newResolveInfo
        + '\''
        + '}';
  }
}
