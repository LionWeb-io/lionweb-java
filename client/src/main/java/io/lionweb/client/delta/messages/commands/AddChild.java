package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedChunk;

public class AddChild extends DeltaCommand {
  public String parent;
  public SerializedChunk newChild;
  public MetaPointer containment;
  public int index;

  public AddChild(
      String commandId,
      String parent,
      SerializedChunk newChild,
      MetaPointer containment,
      int index) {
    super(commandId);
    this.parent = parent;
    this.newChild = newChild;
    this.containment = containment;
    this.index = index;
  }
}
