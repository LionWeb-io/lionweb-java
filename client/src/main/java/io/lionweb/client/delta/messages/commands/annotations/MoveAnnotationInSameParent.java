package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedAnnotation within the same parent to newIndex. */
public final class MoveAnnotationInSameParent extends DeltaCommand {
  public final int newIndex;
  public final @NotNull String movedAnnotation;

  public MoveAnnotationInSameParent(
      @NotNull String commandId, int newIndex, @NotNull String movedAnnotation) {
    super(commandId);
    Objects.requireNonNull(movedAnnotation, "movedAnnotation must not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
  }

  @Override
  public String toString() {
    return "MoveAnnotationInSameParent{"
        + "newIndex="
        + newIndex
        + ", movedAnnotation='"
        + movedAnnotation
        + '\''
        + ", commandId='"
        + commandId
        + '\''
        + ", protocolMessages="
        + protocolMessages
        + '}';
  }
}
