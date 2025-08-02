package io.lionweb.delta.messages.commands;

import io.lionweb.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;

import javax.annotation.Nullable;

public class DeleteProperty extends DeltaCommand {
    public String node;
    public MetaPointer property;
}
