package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;

public class AddChild extends DeltaCommand {
  public String parent;
  public SerializationChunk newChild;
  public MetaPointer containment;
  public int index;

  public AddChild(
      String commandId,
      String parent,
      SerializationChunk newChild,
      MetaPointer containment,
      int index) {
    super(commandId);
    this.parent = parent;
    this.newChild = newChild;
    this.containment = containment;
    this.index = index;
  }
}
