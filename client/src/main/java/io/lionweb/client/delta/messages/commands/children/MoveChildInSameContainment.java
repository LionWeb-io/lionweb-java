package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedChild within its current containment to newIndex. */
public final class MoveChildInSameContainment extends DeltaCommand {
  public final int newIndex;
  public final @NotNull String movedChild;
  public final @NotNull String parent;
  public final @NotNull MetaPointer containment;
  public final int oldIndex;

  public MoveChildInSameContainment(
      @NotNull String commandId,
      int newIndex,
      @NotNull String movedChild,
      @NotNull String parent,
      @NotNull MetaPointer containment,
      int oldIndex) {
    super(commandId);
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    Objects.requireNonNull(movedChild, "movedChild must not be null");
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(containment, "containment must not be null");
    this.newIndex = newIndex;
    this.movedChild = movedChild;
    this.parent = parent;
    this.containment = containment;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "MoveChildInSameContainment{"
        + "newIndex="
        + newIndex
        + ", movedChild='"
        + movedChild
        + '\''
        + ", parent='"
        + parent
        + '\''
        + ", containment="
        + containment
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
