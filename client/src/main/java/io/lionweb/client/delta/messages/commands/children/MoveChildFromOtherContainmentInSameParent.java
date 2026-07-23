package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedChild (currently inside one of movedChild's parent’s containments other
 * than newContainment) inside movedChild's parent’s newContainment at newIndex.
 */
public final class MoveChildFromOtherContainmentInSameParent extends DeltaCommand {
  public final @NotNull MetaPointer newContainment;
  public final int newIndex;
  public final @NotNull String movedChild;
  public final @NotNull String parent;
  public final @NotNull MetaPointer oldContainment;
  public final int oldIndex;

  public MoveChildFromOtherContainmentInSameParent(
      @NotNull String commandId,
      @NotNull MetaPointer newContainment,
      int newIndex,
      @NotNull String movedChild,
      @NotNull String parent,
      @NotNull MetaPointer oldContainment,
      int oldIndex) {
    super(commandId);
    Objects.requireNonNull(newContainment, "newContainment must not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    Objects.requireNonNull(movedChild, "movedChild must not be null");
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(oldContainment, "oldContainment must not be null");
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.movedChild = movedChild;
    this.parent = parent;
    this.oldContainment = oldContainment;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "MoveChildFromOtherContainmentInSameParent{"
        + "newContainment="
        + newContainment
        + ", newIndex="
        + newIndex
        + ", movedChild='"
        + movedChild
        + '\''
        + '}';
  }
}
