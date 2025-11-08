package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.List;

public final class ChildMovedAndReplacedInSameContainment extends BaseDeltaEvent {
  public final int newIndex;
  public final String movedChild;
  public final String parent;
  public final MetaPointer containment;
  public final int oldIndex;
  public final String replacedChild;
  public final List<String> replacedDescendants;

  public ChildMovedAndReplacedInSameContainment(
      int sequenceNumber,
      int newIndex,
      String movedChild,
      String parent,
      MetaPointer containment,
      int oldIndex,
      String replacedChild,
      List<String> replacedDescendants) {
    super(sequenceNumber);
    this.newIndex = newIndex;
    this.movedChild = movedChild;
    this.parent = parent;
    this.containment = containment;
    this.oldIndex = oldIndex;
    this.replacedChild = replacedChild;
    this.replacedDescendants = replacedDescendants;
  }

  @Override
  public String toString() {
    return "ChildMovedAndReplacedInSameContainment{"
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
        + ", replacedChild='"
        + replacedChild
        + '\''
        + ", replacedDescendants="
        + replacedDescendants
        + '}';
  }
}
