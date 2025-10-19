package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import org.jetbrains.annotations.NotNull;

/** Move existing node movedAnnotation inside newParent's annotations at newIndex. */
public class MoveAnnotationFromOtherParent extends DeltaCommand {
  public String newParent;
  public int newIndex;
  public String movedAnnotation;

  public MoveAnnotationFromOtherParent(
      @NotNull String commandId, String newParent, int newIndex, String movedAnnotation) {
    super(commandId);
    this.newParent = newParent;
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
  }
}
