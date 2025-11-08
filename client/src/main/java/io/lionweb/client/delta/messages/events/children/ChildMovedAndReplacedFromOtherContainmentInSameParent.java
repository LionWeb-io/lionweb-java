package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.List;

public final class ChildMovedAndReplacedFromOtherContainmentInSameParent extends BaseDeltaEvent {
  public final MetaPointer newContainment;
  public final int newIndex;
  public final String movedChild;
  public final String parent;
  public final MetaPointer oldContainment;
  public final int oldIndex;
  public final String replacedChild;
  public final List<String> replacedDescendants;

  public ChildMovedAndReplacedFromOtherContainmentInSameParent(
      int sequenceNumber,
      MetaPointer newContainment,
      int newIndex,
      String movedChild,
      String parent,
      MetaPointer oldContainment,
      int oldIndex,
      String replacedChild,
      List<String> replacedDescendants) {
    super(sequenceNumber);
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.movedChild = movedChild;
    this.parent = parent;
    this.oldContainment = oldContainment;
    this.oldIndex = oldIndex;
    this.replacedChild = replacedChild;
    this.replacedDescendants = replacedDescendants;
  }

  @Override
  public String toString() {
    return "ChildMovedAndReplacedFromOtherContainmentInSameParent{"
        + "newContainment="
        + newContainment
        + ", newIndex="
        + newIndex
        + ", movedChild='"
        + movedChild
        + '\''
        + ", parent='"
        + parent
        + '\''
        + ", oldContainment="
        + oldContainment
        + ", oldIndex="
        + oldIndex
        + ", replacedChild='"
        + replacedChild
        + '\''
        + ", replacedDescendants="
        + replacedDescendants
        + '}';
  }
}
