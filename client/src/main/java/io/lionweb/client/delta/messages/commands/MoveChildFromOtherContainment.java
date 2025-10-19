package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;

public class MoveChildFromOtherContainment extends DeltaCommand {
  public String newParent;
  public MetaPointer newContainment;
  public int newIndex;
  public String movedChild;

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
