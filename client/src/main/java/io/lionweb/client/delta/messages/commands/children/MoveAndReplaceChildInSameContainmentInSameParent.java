package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedChild, initially at index oldIndex within the containment inside parent,
 * to the target index (oldIndex + indexOffset), replacing current child replacedChild at the target
 * index. replacedChild subsequently gets deleted, including all its descendants (including
 * annotation instances). Does NOT change references to any of the deleted nodes.
 */
public final class MoveAndReplaceChildInSameContainmentInSameParent extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer containment;
  public final int indexOffset;
  public final int oldIndex;
  public final @NotNull String replacedChild;
  public final @NotNull String movedChild;

  public MoveAndReplaceChildInSameContainmentInSameParent(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer containment,
      int indexOffset,
      int oldIndex,
      @NotNull String replacedChild,
      @NotNull String movedChild) {
    super(commandId);
    Objects.requireNonNull(replacedChild, "replacedChild must not be null");
    Objects.requireNonNull(movedChild, "movedChild must not be null");
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(containment, "containment must not be null");
    this.indexOffset = indexOffset;
    this.replacedChild = replacedChild;
    this.movedChild = movedChild;
    this.parent = parent;
    this.containment = containment;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceChildInSameContainmentInSameParent{"
        + "parent='"
        + parent
        + '\''
        + ", containment="
        + containment
        + ", indexOffset="
        + indexOffset
        + ", oldIndex="
        + oldIndex
        + ", replacedChild='"
        + replacedChild
        + '\''
        + ", movedChild='"
        + movedChild
        + '\''
        + ", additionalInfos="
        + additionalInfos
        + ", split="
        + split
        + ", commandId='"
        + commandId
        + '\''
        + '}';
  }
}
