package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public final class ChildMovedFromOtherContainment extends CommonDeltaEvent {
  public final String newParent;
  public final MetaPointer newContainment;
  public final int newIndex;
  public final String movedChild;

  public ChildMovedFromOtherContainment(
      int sequenceNumber,
      String newParent,
      MetaPointer newContainment,
      int newIndex,
      String movedChild) {
    super(sequenceNumber);
    this.newParent = newParent;
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.movedChild = movedChild;
  }
}
