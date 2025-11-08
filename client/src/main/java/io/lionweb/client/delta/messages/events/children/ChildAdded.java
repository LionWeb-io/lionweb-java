package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;

public final class ChildAdded extends BaseDeltaEvent<ChildAdded> {
  public final String parent;
  public final SerializationChunk newChild;
  public final MetaPointer containment;
  public final int index;

  public ChildAdded(
      int sequenceNumber,
      String parent,
      SerializationChunk newChild,
      MetaPointer containment,
      int index) {
    super(sequenceNumber);
    this.parent = parent;
    this.newChild = newChild;
    this.containment = containment;
    this.index = index;
  }

  @Override
  public String toString() {
    return "ChildAdded{"
        + "parent='"
        + parent
        + '\''
        + ", newChild="
        + newChild
        + ", containment="
        + containment
        + ", index="
        + index
        + '}';
  }
}
