package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedAnnotation inside newParent's annotations at newIndex. Delete current
 * node replacedAnnotation at newParent's annotations at newIndex, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes
 */
public final class MoveAndReplaceAnnotationFromOtherParent extends DeltaCommand {
  public final @NotNull String newParent;
  public final int newIndex;
  public final @NotNull String replacedAnnotation;
  public final @NotNull String movedAnnotation;

  public MoveAndReplaceAnnotationFromOtherParent(
      @NotNull String commandId,
      @NotNull String newParent,
      int newIndex,
      @NotNull String replacedAnnotation,
      @NotNull String movedAnnotation) {
    super(commandId);
    Objects.requireNonNull(newParent, "newParent must not be null");
    Objects.requireNonNull(replacedAnnotation, "replacedAnnotation must not be null");
    Objects.requireNonNull(movedAnnotation, "movedAnnotation must not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    this.newParent = newParent;
    this.newIndex = newIndex;
    this.replacedAnnotation = replacedAnnotation;
    this.movedAnnotation = movedAnnotation;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceAnnotationFromOtherParent{"
        + "newParent='"
        + newParent
        + '\''
        + ", newIndex="
        + newIndex
        + ", replacedAnnotation='"
        + replacedAnnotation
        + '\''
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
