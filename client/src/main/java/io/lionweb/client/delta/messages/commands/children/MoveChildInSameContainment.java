package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedChild within its current containment to newIndex. */
public final class MoveChildInSameContainment extends DeltaCommand {
  public final int newIndex;
  public final @NotNull String movedChild;

  public MoveChildInSameContainment(
      @NotNull String commandId, int newIndex, @NotNull String movedChild) {
    super(commandId);
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    Objects.requireNonNull(movedChild, "movedChild must not be null");
    this.newIndex = newIndex;
    this.movedChild = movedChild;
  }

  @Override
  public String toString() {
    return "MoveChildInSameContainment{"
        + "newIndex="
        + newIndex
        + ", movedChild='"
        + movedChild
        + '\''
        + '}';
  }
}
