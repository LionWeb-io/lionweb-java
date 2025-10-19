package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;

/** Move existing node movedChild within its current containment to newIndex. */
public class MoveChildInSameContainment extends DeltaCommand {
  public int newIndex;
  public String movedChild;

  public MoveChildInSameContainment(String commandId, int newIndex, String movedChild) {
    super(commandId);
    this.newIndex = newIndex;
    this.movedChild = movedChild;
  }
}
