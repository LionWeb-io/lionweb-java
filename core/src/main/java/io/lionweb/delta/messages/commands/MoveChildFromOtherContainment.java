package io.lionweb.delta.messages.commands;

import io.lionweb.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedChunk;

public class MoveChildFromOtherContainment extends DeltaCommand {
    public String newParent;
    public MetaPointer newContainment;
    public int newIndex;
    public String movedChild;
}
