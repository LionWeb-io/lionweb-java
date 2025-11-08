package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public final class ChildDeleted extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer containment;
  public final int index;
  public final String deletedChild;

  public ChildDeleted(
      int sequenceNumber, String parent, MetaPointer containment, int index, String deletedChild) {
    super(sequenceNumber);
    this.parent = parent;
    this.containment = containment;
    this.index = index;
    this.deletedChild = deletedChild;
  }

  @Override
  public String toString() {
    return "ChildDeleted{"
        + "parent='"
        + parent
        + '\''
        + ", containment="
        + containment
        + ", index="
        + index
        + ", deletedChild='"
        + deletedChild
        + '\''
        + '}';
  }
}
