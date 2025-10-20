package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;

/**
 * Move existing node movedChild within its current containment to newIndex. Delete current child
 * replacedChild inside the same containment at newIndex, and all its descendants (including
 * annotation instances). Does NOT change references to any of the deleted nodes
 */
public final class MoveAndReplaceChildInSameContainment extends DeltaCommand {
  public final int newIndex;
  public final String replacedChild;
  public final String movedChild;

  public MoveAndReplaceChildInSameContainment(
      String commandId, int newIndex, String replacedChild, String movedChild) {
    super(commandId);
    this.newIndex = newIndex;
    this.replacedChild = replacedChild;
    this.movedChild = movedChild;
  }
}
