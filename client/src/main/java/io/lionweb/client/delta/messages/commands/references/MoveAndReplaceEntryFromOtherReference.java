package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move existing entry movedTarget/movedResolveInfo inside oldParent's oldReference at oldIndex to
 * newParent's newReference at newIndex, replacing existing entry replacedTarget/replacedResolveInfo
 * in newParent's newReference at newIndex.
 */
public final class MoveAndReplaceEntryFromOtherReference extends DeltaCommand {
  public final @Nullable String newParent;
  public final @NotNull MetaPointer newReference;
  public final int newIndex;
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
      int newIndex,
      @Nullable String replacedTarget,
      @Nullable String replacedResolveInfo,
      @NotNull String oldParent,
      @NotNull MetaPointer oldReference,
      int oldIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(commandId);
    Objects.requireNonNull(newReference, "newReference must not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    Objects.requireNonNull(oldReference, "oldReference must not be null");
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    Objects.requireNonNull(newParent, "newParent must not be null");
    Objects.requireNonNull(oldParent, "oldParent must not be null");
    this.newParent = newParent;
    this.newReference = newReference;
    this.newIndex = newIndex;
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
        + ", newIndex="
        + newIndex
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
