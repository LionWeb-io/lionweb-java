package io.lionweb.delta.messages.commands;

import io.lionweb.delta.messages.DeltaCommand;
import io.lionweb.delta.messages.ProtocolMessage;
import io.lionweb.serialization.data.SerializedChunk;

import java.util.List;

public class DeletePartition extends DeltaCommand {
    private String deletedPartition;
}
