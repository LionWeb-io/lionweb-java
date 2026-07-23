package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move existing entry movedTarget/movedResolveInfo inside oldParent's oldReference at oldIndex to
 * newParent's newReference at indexOffset, replacing existing entry
 * replacedTarget/replacedResolveInfo in newParent's newReference at indexOffset.
 */
public final class MoveAndReplaceEntryFromOtherReference extends DeltaCommand {
  public final @Nullable String newParent;
  public final @NotNull MetaPointer newReference;
  public final int indexOffset;
  public final @Nullable String replacedTarget;
  public final @Nullable String replacedResolveInfo;
  public final @Nullable String oldParent;
  public final @NotNull MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public MoveAndReplaceEntryFromOtherReference(
      @NotNull String commandId,
      @NotNull String newParent,
      @NotNull MetaPointer newReference,
      int indexOffset,
      @Nullable String replacedTarget,
      @Nullable String replacedResolveInfo,
      @NotNull String oldParent,
      @NotNull MetaPointer oldReference,
      int oldIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(commandId);
    Objects.requireNonNull(newReference, "newReference must not be null");
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset must be non-negative");
    }
    Objects.requireNonNull(oldReference, "oldReference must not be null");
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    Objects.requireNonNull(newParent, "newParent must not be null");
    Objects.requireNonNull(oldParent, "oldParent must not be null");
    this.newParent = newParent;
    this.newReference = newReference;
    this.indexOffset = indexOffset;
    this.replacedTarget = replacedTarget;
    this.replacedResolveInfo = replacedResolveInfo;
    this.oldParent = oldParent;
    this.oldReference = oldReference;
    this.oldIndex = oldIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceEntryFromOtherReference{"
        + "newParent='"
        + newParent
        + '\''
        + ", newReference="
        + newReference
        + ", indexOffset="
        + indexOffset
        + ", replacedTarget='"
        + replacedTarget
        + '\''
        + ", replacedResolveInfo='"
        + replacedResolveInfo
        + '\''
        + ", oldParent='"
        + oldParent
        + '\''
        + ", oldReference="
        + oldReference
        + ", oldIndex="
        + oldIndex
        + ", movedTarget='"
        + movedTarget
        + '\''
        + ", movedResolveInfo='"
        + movedResolveInfo
        + '\''
        + '}';
  }
}
