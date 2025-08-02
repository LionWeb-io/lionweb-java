package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.SerializedChunk;

public class AddPartition extends DeltaCommand {
    public SerializedChunk newPartition;

    public AddPartition(String commandId, SerializedChunk newPartition) {
        super(commandId);
        this.newPartition = newPartition;
    }
}
