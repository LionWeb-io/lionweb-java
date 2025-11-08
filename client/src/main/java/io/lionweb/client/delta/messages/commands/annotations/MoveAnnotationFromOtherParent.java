package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedAnnotation inside newParent's annotations at newIndex. */
public final class MoveAnnotationFromOtherParent extends DeltaCommand {
  public final @NotNull String newParent;
  public final int newIndex;
  public final @NotNull String movedAnnotation;

  public MoveAnnotationFromOtherParent(
      @NotNull String commandId,
      @NotNull String newParent,
      int newIndex,
      @NotNull String movedAnnotation) {
    super(commandId);
    Objects.requireNonNull(newParent, "newParent must not be null");
    Objects.requireNonNull(movedAnnotation, "movedAnnotation must not be null");
    this.newParent = newParent;
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
  }

  @Override
  public String toString() {
    return "MoveAnnotationFromOtherParent{"
        + "newParent='"
        + newParent
        + '\''
        + ", newIndex="
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
