package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedAnnotation within the same parent to newIndex. Delete current node
 * replacedAnnotation at movedAnnotation's parent’s annotations at newIndex, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes.
 */
public final class MoveAndReplaceAnnotationInSameParent extends DeltaCommand {
  public final int newIndex;
  public final @NotNull String replacedAnnotation;
  public final @NotNull String movedAnnotation;

  public MoveAndReplaceAnnotationInSameParent(
      @NotNull String commandId,
      int newIndex,
      @NotNull String replacedAnnotation,
      @NotNull String movedAnnotation) {
    super(commandId);
    Objects.requireNonNull(replacedAnnotation, "replacedAnnotation must not be null");
    Objects.requireNonNull(movedAnnotation, "movedAnnotation must not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    this.newIndex = newIndex;
    this.replacedAnnotation = replacedAnnotation;
    this.movedAnnotation = movedAnnotation;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceAnnotationInSameParent{"
        + "newIndex="
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
