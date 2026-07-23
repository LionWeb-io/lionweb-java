package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedAnnotation within the same parent to indexOffset. Delete current node
 * replacedAnnotation at movedAnnotation's parent’s annotations at indexOffset, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes.
 */
public final class MoveAndReplaceAnnotationInSameParent extends DeltaCommand {
  public final int indexOffset;
  public final @NotNull String replacedAnnotation;
  public final @NotNull String movedAnnotation;
  public final @NotNull String parent;
  public final int oldIndex;

  public MoveAndReplaceAnnotationInSameParent(
      @NotNull String commandId,
      int indexOffset,
      @NotNull String replacedAnnotation,
      @NotNull String movedAnnotation,
      @NotNull String parent,
      int oldIndex) {
    super(commandId);
    Objects.requireNonNull(replacedAnnotation, "replacedAnnotation must not be null");
    Objects.requireNonNull(movedAnnotation, "movedAnnotation must not be null");
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset must be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    Objects.requireNonNull(parent, "parent must not be null");
    this.indexOffset = indexOffset;
    this.replacedAnnotation = replacedAnnotation;
    this.movedAnnotation = movedAnnotation;
    this.parent = parent;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "MoveAndReplaceAnnotationInSameParent{"
        + "indexOffset="
        + indexOffset
        + ", replacedAnnotation='"
        + replacedAnnotation
        + '\''
        + ", movedAnnotation='"
        + movedAnnotation
        + '\''
        + ", parent='"
        + parent
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
