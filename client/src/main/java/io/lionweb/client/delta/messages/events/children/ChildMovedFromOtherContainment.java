package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public final class ChildMovedFromOtherContainment extends BaseDeltaEvent {
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

  @Override
  public String toString() {
    return "ChildMovedFromOtherContainment{"
        + "newParent='"
        + newParent
        + '\''
        + ", newContainment="
        + newContainment
        + ", newIndex="
        + newIndex
        + ", movedChild='"
        + movedChild
        + '\''
        + '}';
  }
}
