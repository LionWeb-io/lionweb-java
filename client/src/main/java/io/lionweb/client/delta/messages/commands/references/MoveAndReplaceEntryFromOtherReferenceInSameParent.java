package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move existing entry movedTarget/movedResolveInfo inside parent's oldReference at oldIndex to
 * parent's newReference at newIndex, replacing existing entry
 * replacedTarget/replacedResolveInfo[32] in parent's newReference at newIndex.
 */
public final class MoveAndReplaceEntryFromOtherReferenceInSameParent extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer newReference;
  public final int newIndex;
  public final @Nullable String replacedTarget;
  public final @Nullable String replacedResolveInfo;
  public final @NotNull MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public MoveAndReplaceEntryFromOtherReferenceInSameParent(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer newReference,
      int newIndex,
      @Nullable String replacedTarget,
      @Nullable String replacedResolveInfo,
      @NotNull MetaPointer oldReference,
      int oldIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(commandId);
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(newReference, "newReference must not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    Objects.requireNonNull(oldReference, "oldReference must not be null");
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    this.parent = parent;
    this.newReference = newReference;
    this.newIndex = newIndex;
    this.replacedTarget = replacedTarget;
    this.replacedResolveInfo = replacedResolveInfo;
    this.oldReference = oldReference;
    this.oldIndex = oldIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceEntryFromOtherReferenceInSameParent{"
        + "parent='"
        + parent
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
