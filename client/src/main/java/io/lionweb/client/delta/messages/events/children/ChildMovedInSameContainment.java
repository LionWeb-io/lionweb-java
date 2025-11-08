package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public final class ChildMovedInSameContainment extends BaseDeltaEvent {
  public final int newIndex;
  public final String movedChild;
  public final String parent;
  public final MetaPointer containment;
  public final int oldIndex;

  public ChildMovedInSameContainment(
      int sequenceNumber,
      int newIndex,
      String movedChild,
      String parent,
      MetaPointer containment,
      int oldIndex) {
    super(sequenceNumber);
    this.newIndex = newIndex;
    this.movedChild = movedChild;
    this.parent = parent;
    this.containment = containment;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "ChildMovedInSameContainment{"
        + "newIndex="
        + newIndex
        + ", movedChild='"
        + movedChild
        + '\''
        + ", parent='"
        + parent
        + '\''
        + ", containment="
        + containment
        + ", oldIndex="
        + oldIndex
        + '}';
  }
}
