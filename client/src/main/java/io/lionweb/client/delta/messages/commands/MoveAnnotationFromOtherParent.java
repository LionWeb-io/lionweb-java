package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedAnnotation inside newParent's annotations at newIndex. */
public final class MoveAnnotationFromOtherParent extends DeltaCommand {
  public final String newParent;
  public final int newIndex;
  public final String movedAnnotation;

  public MoveAnnotationFromOtherParent(
      @NotNull String commandId, String newParent, int newIndex, String movedAnnotation) {
    super(commandId);
    this.newParent = newParent;
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
  }
}
