package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedAnnotation inside newParent's annotations at indexOffset. Delete current
 * node replacedAnnotation at newParent's annotations at indexOffset, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes
 */
public final class MoveAndReplaceAnnotationFromOtherParent extends DeltaCommand {
  public final @NotNull String newParent;
  public final int indexOffset;
  public final @NotNull String replacedAnnotation;
  public final @NotNull String movedAnnotation;
  public final @NotNull String oldParent;
  public final int oldIndex;

  public MoveAndReplaceAnnotationFromOtherParent(
      @NotNull String commandId,
      @NotNull String newParent,
      int indexOffset,
      @NotNull String replacedAnnotation,
      @NotNull String movedAnnotation,
      @NotNull String oldParent,
      int oldIndex) {
    super(commandId);
    Objects.requireNonNull(newParent, "newParent must not be null");
    Objects.requireNonNull(replacedAnnotation, "replacedAnnotation must not be null");
    Objects.requireNonNull(movedAnnotation, "movedAnnotation must not be null");
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset must be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    Objects.requireNonNull(oldParent, "oldParent must not be null");
    this.newParent = newParent;
    this.indexOffset = indexOffset;
    this.replacedAnnotation = replacedAnnotation;
    this.movedAnnotation = movedAnnotation;
    this.oldParent = oldParent;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceAnnotationFromOtherParent{"
        + "newParent='"
        + newParent
        + '\''
        + ", indexOffset="
        + indexOffset
        + ", replacedAnnotation='"
        + replacedAnnotation
        + '\''
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
