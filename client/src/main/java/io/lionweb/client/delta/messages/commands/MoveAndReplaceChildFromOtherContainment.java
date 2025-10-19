package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;

/**
 * Move existing node movedChild inside newParent's newContainment at newIndex. Delete current child
 * replacedChild inside newParent's newContainment at newIndex, and all its descendants (including
 * annotation instances). Does NOT change references to any of the deleted nodes.
 */
public final class MoveAndReplaceChildFromOtherContainment extends DeltaCommand {
  public final String newParent;
  public final MetaPointer newContainment;
  public final int newIndex;
  public final String replacedChild;
  public final String movedChild;

  public MoveAndReplaceChildFromOtherContainment(
      String commandId,
      String newParent,
      MetaPointer newContainment,
      int newIndex,
      String replacedChild,
      String movedChild) {
    super(commandId);
    this.newParent = newParent;
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.replacedChild = replacedChild;
    this.movedChild = movedChild;
  }
}
