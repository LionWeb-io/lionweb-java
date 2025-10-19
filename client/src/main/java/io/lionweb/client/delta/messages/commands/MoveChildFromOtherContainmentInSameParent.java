package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;

/**
 * Move existing node movedChild (currently inside one of movedChild's parent’s containments other
 * than newContainment) inside movedChild's parent’s newContainment at newIndex.
 */
public class MoveChildFromOtherContainmentInSameParent extends DeltaCommand {
  public MetaPointer newContainment;
  public int newIndex;
  public String movedChild;

  public MoveChildFromOtherContainmentInSameParent(
      String commandId, MetaPointer newContainment, int newIndex, String movedChild) {
    super(commandId);
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.movedChild = movedChild;
  }
}
