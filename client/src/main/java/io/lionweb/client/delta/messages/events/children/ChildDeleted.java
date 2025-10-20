package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public final class ChildDeleted extends CommonDeltaEvent {
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
}
