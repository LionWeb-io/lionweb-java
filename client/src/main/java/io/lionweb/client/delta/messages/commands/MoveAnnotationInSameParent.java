package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedAnnotation within the same parent to newIndex. */
public final class MoveAnnotationInSameParent extends DeltaCommand {
  public final int newIndex;
  public final String movedAnnotation;

  public MoveAnnotationInSameParent(
      @NotNull String commandId, int newIndex, String movedAnnotation) {
    super(commandId);
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
  }
}
