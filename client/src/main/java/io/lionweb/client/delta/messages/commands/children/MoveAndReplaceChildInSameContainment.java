package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedChild within its current containment to newIndex. Delete current child
 * replacedChild inside the same containment at newIndex, and all its descendants (including
 * annotation instances). Does NOT change references to any of the deleted nodes
 */
public final class MoveAndReplaceChildInSameContainment extends DeltaCommand {
  public final int newIndex;
  public final @NotNull String replacedChild;
  public final @NotNull String movedChild;

  public MoveAndReplaceChildInSameContainment(
      @NotNull String commandId,
      int newIndex,
      @NotNull String replacedChild,
      @NotNull String movedChild) {
    super(commandId);
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    Objects.requireNonNull(replacedChild, "replacedChild must not be null");
    Objects.requireNonNull(movedChild, "movedChild must not be null");
    this.newIndex = newIndex;
    this.replacedChild = replacedChild;
    this.movedChild = movedChild;
  }
}
