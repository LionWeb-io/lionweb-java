package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedAnnotation within the same parent to newIndex. */
public final class MoveAnnotationInSameParent extends DeltaCommand {
  public final int newIndex;
  public final @NotNull String movedAnnotation;
  public final @NotNull String parent;
  public final int oldIndex;

  public MoveAnnotationInSameParent(
      @NotNull String commandId,
      int newIndex,
      @NotNull String movedAnnotation,
      @NotNull String parent,
      int oldIndex) {
    super(commandId);
    Objects.requireNonNull(movedAnnotation, "movedAnnotation must not be null");
    Objects.requireNonNull(parent, "parent must not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex must be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex must be non-negative");
    }
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
    this.parent = parent;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "MoveAnnotationInSameParent{"
        + "newIndex="
        + newIndex
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
