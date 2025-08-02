package io.lionweb.delta.messages.commands;

import io.lionweb.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedChunk;

public class AddChild extends DeltaCommand {
    private String parent;
    private SerializedChunk newChild;
    private MetaPointer containment;
    private int index;

}
