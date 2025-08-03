package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;

public class DeletePartition extends DeltaCommand {
  public String deletedPartition;

  public DeletePartition(String commandId, String deletedPartition) {
    super(commandId);
    this.deletedPartition = deletedPartition;
  }
}
