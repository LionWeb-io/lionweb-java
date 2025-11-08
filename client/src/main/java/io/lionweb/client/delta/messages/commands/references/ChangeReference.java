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
  public final @Nullable String oldTarget;
  public final @Nullable String oldResolveInfo;
  public final @Nullable String newTarget;
  public final @Nullable String newResolveInfo;

  public ChangeReference(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String oldTarget,
      @Nullable String oldResolveInfo,
      @Nullable String newTarget,
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
    this.oldTarget = oldTarget;
    this.oldResolveInfo = oldResolveInfo;
    this.newTarget = newTarget;
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
        + ", oldTarget='"
        + oldTarget
        + '\''
        + ", oldResolveInfo='"
        + oldResolveInfo
        + '\''
        + ", newTarget='"
        + newTarget
        + '\''
        + ", newResolveInfo='"
        + newResolveInfo
        + '\''
        + '}';
  }
}
