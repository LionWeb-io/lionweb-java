package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move existing entry movedTarget/movedResolveInfo[32] inside parent's reference at oldIndex inside
 * parent's reference at newIndex, replacing existing entry replacedTarget/replacedResolveInfo[32]
 * in parent's reference at newIndex.
 */
public final class MoveAndReplaceEntryInSameReference extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int oldIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;
  public final int newIndex;
  public final @Nullable String replacedTarget;
  public final @Nullable String replacedResolveInfo;

  public MoveAndReplaceEntryInSameReference(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int oldIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo,
      int newIndex,
      @Nullable String replacedTarget,
      @Nullable String replacedResolveInfo) {
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
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
    this.newIndex = newIndex;
    this.replacedTarget = replacedTarget;
    this.replacedResolveInfo = replacedResolveInfo;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceEntryInSameReference{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", oldIndex="
        + oldIndex
        + ", movedTarget='"
        + movedTarget
        + '\''
        + ", movedResolveInfo='"
        + movedResolveInfo
        + '\''
        + ", newIndex="
        + newIndex
        + ", replacedTarget='"
        + replacedTarget
        + '\''
        + ", replacedResolveInfo='"
        + replacedResolveInfo
        + '\''
        + '}';
  }
}
