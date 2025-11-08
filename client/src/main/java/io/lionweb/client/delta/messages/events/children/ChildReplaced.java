package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;

public final class ChildReplaced extends BaseDeltaEvent {
  public final SerializationChunk newChild;
  public final String parent;
  public final MetaPointer containment;
  public final int index;
  public final String replacedChild;

  public ChildReplaced(
      int sequenceNumber,
      SerializationChunk newChild,
      String parent,
      MetaPointer containment,
      int index,
      String replacedChild) {
    super(sequenceNumber);
    this.newChild = newChild;
    this.parent = parent;
    this.containment = containment;
    this.index = index;
    this.replacedChild = replacedChild;
  }

  @Override
  public String toString() {
    return "ChildReplaced{"
        + "newChild="
        + newChild
        + ", parent='"
        + parent
        + '\''
        + ", containment="
        + containment
        + ", index="
        + index
        + ", replacedChild='"
        + replacedChild
        + '\''
        + '}';
  }
}
