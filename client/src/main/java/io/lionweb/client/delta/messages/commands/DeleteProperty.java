package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;

public class DeleteProperty extends DeltaCommand {
  public String node;
  public MetaPointer property;

  public DeleteProperty(String commandId, String node, MetaPointer property) {
    super(commandId);
    this.node = node;
    this.property = property;
  }
}
