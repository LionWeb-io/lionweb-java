package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedChild inside newParent's newContainment at newIndex. */
public final class MoveChildFromOtherContainment extends DeltaCommand {
  public final @NotNull String newParent;
  public final @NotNull MetaPointer newContainment;
  public final int newIndex;
  public final @NotNull String movedChild;

  public MoveChildFromOtherContainment(
      @NotNull String commandId,
      @NotNull String newParent,
      @NotNull MetaPointer newContainment,
      int newIndex,
      @NotNull String movedChild) {
    super(commandId);
    Objects.requireNonNull(newParent, "newParent must not be null");
    Objects.requireNonNull(newContainment, "newContainment must not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    Objects.requireNonNull(movedChild, "movedChild must not be null");
    this.newParent = newParent;
    this.newContainment = newContainment;
    this.newIndex = newIndex;
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
        + ", newIndex="
        + newIndex
        + ", movedChild='"
        + movedChild
        + '\''
        + '}';
  }
}
