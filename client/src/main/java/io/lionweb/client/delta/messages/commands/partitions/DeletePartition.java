package io.lionweb.client.delta.messages.commands.partitions;

import io.lionweb.client.delta.messages.DeltaCommand;

public final class DeletePartition extends DeltaCommand {
  public final String deletedPartition;

  public DeletePartition(String commandId, String deletedPartition) {
    super(commandId);
    this.deletedPartition = deletedPartition;
  }
}
