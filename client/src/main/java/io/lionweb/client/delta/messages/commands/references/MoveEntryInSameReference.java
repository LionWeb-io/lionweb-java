package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move existing entry movedTarget/movedResolveInfo inside parent's reference at oldIndex inside
 * parent's reference at newIndex.
 */
public final class MoveEntryInSameReference extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int oldIndex;
  public final int newIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public MoveEntryInSameReference(
      @NotNull String commandId,
      @Nullable String parent,
      @NotNull MetaPointer reference,
      int oldIndex,
      int newIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(commandId);
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(reference, "reference must not be null");
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    this.parent = parent;
    this.reference = reference;
    this.oldIndex = oldIndex;
    this.newIndex = newIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
  }

  @Override
  public String toString() {
    return "MoveEntryInSameReference{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", oldIndex="
        + oldIndex
        + ", newIndex="
        + newIndex
        + ", movedTarget='"
        + movedTarget
        + '\''
        + ", movedResolveInfo='"
        + movedResolveInfo
        + '\''
        + '}';
  }
}
