package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;

/** Move existing node movedChild inside newParent's newContainment at newIndex. */
public final class MoveChildFromOtherContainment extends DeltaCommand {
  public final String newParent;
  public final MetaPointer newContainment;
  public final int newIndex;
  public final String movedChild;

  public MoveChildFromOtherContainment(
      String commandId,
      String newParent,
      MetaPointer newContainment,
      int newIndex,
      String movedChild) {
    super(commandId);
    this.newParent = newParent;
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.movedChild = movedChild;
  }
}
