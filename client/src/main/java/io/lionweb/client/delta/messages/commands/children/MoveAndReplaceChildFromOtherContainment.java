package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedChild inside newParent's newContainment at indexOffset. Delete current child
 * replacedChild inside newParent's newContainment at indexOffset, and all its descendants (including
 * annotation instances). Does NOT change references to any of the deleted nodes.
 */
public final class MoveAndReplaceChildFromOtherContainment extends DeltaCommand {
  public final @NotNull String newParent;
  public final @NotNull MetaPointer newContainment;
  public final int indexOffset;
  public final @NotNull String oldParent;
  public final @NotNull MetaPointer oldContainment;
  public final int oldIndex;
  public final @NotNull String replacedChild;
  public final @NotNull String movedChild;

  public MoveAndReplaceChildFromOtherContainment(
      @NotNull String commandId,
      @NotNull String newParent,
      @NotNull MetaPointer newContainment,
      int indexOffset,
      @NotNull String oldParent,
      @NotNull MetaPointer oldContainment,
      int oldIndex,
      @NotNull String replacedChild,
      @NotNull String movedChild) {
    super(commandId);
    Objects.requireNonNull(newParent, "newParent must not be null");
    Objects.requireNonNull(newContainment, "newContainment must not be null");
    Objects.requireNonNull(oldParent, "oldParent must not be null");
    Objects.requireNonNull(oldContainment, "oldContainment must not be null");
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset must be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    Objects.requireNonNull(replacedChild, "replacedChild must not be null");
    Objects.requireNonNull(movedChild, "movedChild must not be null");
    this.newParent = newParent;
    this.newContainment = newContainment;
    this.indexOffset = indexOffset;
    this.replacedChild = replacedChild;
    this.movedChild = movedChild;
    this.oldParent = oldParent;
    this.oldContainment = oldContainment;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceChildFromOtherContainment{"
        + "newParent='"
        + newParent
        + '\''
        + ", newContainment="
        + newContainment
        + ", indexOffset="
        + indexOffset
        + ", oldParent='"
        + oldParent
        + '\''
        + ", oldContainment="
        + oldContainment
        + ", oldIndex="
        + oldIndex
        + ", replacedChild='"
        + replacedChild
        + '\''
        + ", movedChild='"
        + movedChild
        + '\''
        + ", commandId='"
        + commandId
        + '\''
        + ", split="
        + split
        + ", additionalInfos="
        + additionalInfos
        + '}';
  }
}
