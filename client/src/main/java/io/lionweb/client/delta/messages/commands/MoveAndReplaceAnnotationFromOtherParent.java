package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedAnnotation inside newParent's annotations at newIndex. Delete current
 * node replacedAnnotation at newParent's annotations at newIndex, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes
 */
public final class MoveAndReplaceAnnotationFromOtherParent extends DeltaCommand {
  public final String newParent;
  public final int newIndex;
  public final String replacedAnnotation;
  public final String movedAnnotation;

  public MoveAndReplaceAnnotationFromOtherParent(
      @NotNull String commandId,
      String newParent,
      int newIndex,
      String replacedAnnotation,
      String movedAnnotation) {
    super(commandId);
    this.newParent = newParent;
    this.newIndex = newIndex;
    this.replacedAnnotation = replacedAnnotation;
    this.movedAnnotation = movedAnnotation;
  }
}
