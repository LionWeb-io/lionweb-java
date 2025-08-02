package io.lionweb.delta.messages.commands;

import io.lionweb.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedChunk;

import javax.annotation.Nullable;

public class AddProperty extends DeltaCommand {
    private String node;
    private MetaPointer property;
    private @Nullable String newValue;
}
