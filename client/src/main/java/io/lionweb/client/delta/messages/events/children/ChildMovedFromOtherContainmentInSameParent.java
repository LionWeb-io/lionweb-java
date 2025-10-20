package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public final class ChildMovedFromOtherContainmentInSameParent extends CommonDeltaEvent {
  public final MetaPointer newContainment;
  public final int newIndex;
  public final String movedChild;

  public ChildMovedFromOtherContainmentInSameParent(
      int sequenceNumber, MetaPointer newContainment, int newIndex, String movedChild) {
    super(sequenceNumber);
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.movedChild = movedChild;
  }
}
