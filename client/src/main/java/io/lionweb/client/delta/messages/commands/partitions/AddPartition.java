package io.lionweb.client.delta.messages.commands.partitions;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.SerializationChunk;

public final class AddPartition extends DeltaCommand {
  public final SerializationChunk newPartition;

  public AddPartition(String commandId, SerializationChunk newPartition) {
    super(commandId);
    this.newPartition = newPartition;
  }
}
