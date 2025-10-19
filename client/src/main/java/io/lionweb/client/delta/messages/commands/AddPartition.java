package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.SerializationChunk;

public class AddPartition extends DeltaCommand {
  public SerializationChunk newPartition;

  public AddPartition(String commandId, SerializationChunk newPartition) {
    super(commandId);
    this.newPartition = newPartition;
  }
}
