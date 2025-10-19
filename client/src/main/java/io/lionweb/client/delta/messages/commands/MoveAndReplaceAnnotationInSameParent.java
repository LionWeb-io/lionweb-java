package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import org.jetbrains.annotations.NotNull;

/**
 * Move existing node movedAnnotation within the same parent to newIndex. Delete current node
 * replacedAnnotation at movedAnnotation's parent’s annotations at newIndex, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes.
 */
public final class MoveAndReplaceAnnotationInSameParent extends DeltaCommand {
  public final int newIndex;
  public final String replacedAnnotation;
  public final String movedAnnotation;

  public MoveAndReplaceAnnotationInSameParent(
      @NotNull String commandId, int newIndex, String replacedAnnotation, String movedAnnotation) {
    super(commandId);
    this.newIndex = newIndex;
    this.replacedAnnotation = replacedAnnotation;
    this.movedAnnotation = movedAnnotation;
  }
}
