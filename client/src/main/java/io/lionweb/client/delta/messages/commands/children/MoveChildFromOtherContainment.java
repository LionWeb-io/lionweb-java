package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedChild inside newParent's newContainment at indexOffset. */
public final class MoveChildFromOtherContainment extends DeltaCommand {
  public final @NotNull String newParent;
  public final @NotNull MetaPointer newContainment;
  public final int indexOffset;
  public final @NotNull String movedChild;
  public final @NotNull String oldParent;
  public final @NotNull MetaPointer oldContainment;
  public final int oldIndex;

  public MoveChildFromOtherContainment(
      @NotNull String commandId,
      @NotNull String newParent,
      @NotNull MetaPointer newContainment,
      int indexOffset,
      @NotNull String oldParent,
      @NotNull MetaPointer oldContainment,
      int oldIndex,
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
    Objects.requireNonNull(movedChild, "movedChild must not be null");
    this.newParent = newParent;
    this.newContainment = newContainment;
    this.indexOffset = indexOffset;
    this.oldParent = oldParent;
    this.oldContainment = oldContainment;
    this.oldIndex = oldIndex;
    this.movedChild = movedChild;
  }

  @Override
  public String toString() {
    return "MoveChildFromOtherContainment{"
        + "newParent='"
        + newParent
        + '\''
        + ", newContainment="
        + newContainment
        + ", indexOffset="
        + indexOffset
        + ", movedChild='"
        + movedChild
        + '\''
        + ", oldParent='"
        + oldParent
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
