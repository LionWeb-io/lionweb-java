package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedAnnotation inside newParent's annotations at newIndex. */
public final class MoveAnnotationFromOtherParent extends DeltaCommand {
  public final @NotNull String newParent;
  public final int newIndex;
  public final @NotNull String movedAnnotation;
  public final @NotNull String oldParent;
  public final int oldIndex;

  public MoveAnnotationFromOtherParent(
      @NotNull String commandId,
      @NotNull String newParent,
      int newIndex,
      @NotNull String movedAnnotation,
      @NotNull String oldParent,
      int oldIndex) {
    super(commandId);
    Objects.requireNonNull(newParent, "newParent must not be null");
    Objects.requireNonNull(movedAnnotation, "movedAnnotation must not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    Objects.requireNonNull(oldParent, "oldParent must not be null");
    this.newParent = newParent;
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
    this.oldParent = oldParent;
    this.oldIndex = oldIndex;
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
        + ", oldParent='"
        + oldParent
        + '\''
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
