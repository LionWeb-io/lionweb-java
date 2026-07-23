package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move existing entry movedTarget/movedResolveInfo inside parent's oldReference at oldIndex to
 * parent's newReference at indexOffset.
 */
public final class MoveEntryFromOtherReferenceInSameParent extends DeltaCommand {
  public final @Nullable String parent;
  public final @NotNull MetaPointer newReference;
  public final int indexOffset;
  public final @NotNull MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public MoveEntryFromOtherReferenceInSameParent(
      @Nullable String commandId,
      @NotNull String parent,
      @NotNull MetaPointer newReference,
      int indexOffset,
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
    this.parent = parent;
    this.newReference = newReference;
    this.indexOffset = indexOffset;
    this.oldReference = oldReference;
    this.oldIndex = oldIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
  }

  @Override
  public String toString() {
    return "MoveEntryFromOtherReferenceInSameParent{"
        + "parent='"
        + parent
        + '\''
        + ", newReference="
        + newReference
        + ", indexOffset="
        + indexOffset
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
