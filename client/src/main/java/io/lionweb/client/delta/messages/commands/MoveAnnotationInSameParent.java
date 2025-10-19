package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedAnnotation within the same parent to newIndex. */
public class MoveAnnotationInSameParent extends DeltaCommand {
  public int newIndex;
  public String movedAnnotation;

  public MoveAnnotationInSameParent(
      @NotNull String commandId, int newIndex, String movedAnnotation) {
    super(commandId);
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
  }
}
