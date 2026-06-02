package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildMovedInSameContainment extends BaseDeltaEvent {
  public final int newIndex;
  public final String movedChild;
  public final String parent;
  public final MetaPointer containment;
  public final int oldIndex;

  public ChildMovedInSameContainment(
      int sequenceNumber,
      int newIndex,
      @NotNull String movedChild,
      @NotNull String parent,
      @NotNull MetaPointer containment,
      int oldIndex) {
    super(sequenceNumber);
    Objects.requireNonNull(movedChild, "movedChild cannot be null");
    Objects.requireNonNull(parent, "parent cannot be null");
    Objects.requireNonNull(containment, "containment cannot be null");
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
