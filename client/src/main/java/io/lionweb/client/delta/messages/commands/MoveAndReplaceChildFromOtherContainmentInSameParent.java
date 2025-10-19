package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;

/**
 * Move existing node movedChild (currently inside one of movedChild's parent’s containments other
 * than newContainment) inside movedChild's parent’s newContainment at newIndex. Delete current
 * child replacedChild inside movedChild's parent’s newContainment at newIndex, and all its
 * descendants (including annotation instances). Does NOT change references to any of the deleted
 * nodes.
 */
public final class MoveAndReplaceChildFromOtherContainmentInSameParent extends DeltaCommand {
  public final MetaPointer newContainment;
  public final int newIndex;
  public final String replacedChild;
  public final String movedChild;

  public MoveAndReplaceChildFromOtherContainmentInSameParent(
      String commandId,
      MetaPointer newContainment,
      int newIndex,
      String replacedChild,
      String movedChild) {
    super(commandId);
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.replacedChild = replacedChild;
    this.movedChild = movedChild;
  }
}
