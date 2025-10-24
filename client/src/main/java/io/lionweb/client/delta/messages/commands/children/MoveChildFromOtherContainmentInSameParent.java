package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;

/**
 * Move existing node movedChild (currently inside one of movedChild's parent’s containments other
 * than newContainment) inside movedChild's parent’s newContainment at newIndex.
 */
public final class MoveChildFromOtherContainmentInSameParent extends DeltaCommand {
  public final MetaPointer newContainment;
  public final int newIndex;
  public final String movedChild;

  public MoveChildFromOtherContainmentInSameParent(
      String commandId, MetaPointer newContainment, int newIndex, String movedChild) {
    super(commandId);
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.movedChild = movedChild;
  }
}
