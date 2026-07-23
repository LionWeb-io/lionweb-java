package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedChild (currently inside one of movedChild's parent’s containments other
 * than newContainment) inside movedChild's parent’s newContainment at indexOffset. Delete current
 * child replacedChild inside movedChild's parent’s newContainment at indexOffset, and all its
 * descendants (including annotation instances). Does NOT change references to any of the deleted
 * nodes.
 */
public final class MoveAndReplaceChildFromOtherContainmentInSameParent extends DeltaCommand {
  public final @NotNull MetaPointer newContainment;
  public final int indexOffset;
  public final @NotNull String replacedChild;
  public final @NotNull String movedChild;
  public final @NotNull String parent;
  public final @NotNull MetaPointer oldContainment;
  public final int oldIndex;

  public MoveAndReplaceChildFromOtherContainmentInSameParent(
      @NotNull String commandId,
      @NotNull MetaPointer newContainment,
      int indexOffset,
      @NotNull String replacedChild,
      @NotNull String movedChild,
      @NotNull String parent,
      @NotNull MetaPointer oldContainment,
      int oldIndex) {
    super(commandId);

    Objects.requireNonNull(newContainment, "newContainment must not be null");
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset must be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    Objects.requireNonNull(replacedChild, "replacedChild must not be null");
    Objects.requireNonNull(movedChild, "movedChild must not be null");
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(oldContainment, "oldContainment must not be null");
    this.newContainment = newContainment;
    this.indexOffset = indexOffset;
    this.replacedChild = replacedChild;
    this.movedChild = movedChild;
    this.parent = parent;
    this.oldContainment = oldContainment;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceChildFromOtherContainmentInSameParent{"
        + "newContainment="
        + newContainment
        + ", indexOffset="
        + indexOffset
        + ", replacedChild='"
        + replacedChild
        + '\''
        + ", movedChild='"
        + movedChild
        + '\''
        + ", parent='"
        + parent
        + '\''
        + ", oldContainment="
        + oldContainment
        + ", oldIndex="
        + oldIndex
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
