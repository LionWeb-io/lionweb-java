package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildMovedInSameContainment extends BaseDeltaEvent<ChildMovedInSameContainment> {
  public final int indexOffset;
  public final @NotNull String movedChild;
  public final @NotNull String parent;
  public final @NotNull MetaPointer containment;
  public final int oldIndex;

  public ChildMovedInSameContainment(
      int sequenceNumber,
      int indexOffset,
      @NotNull String movedChild,
      @NotNull String parent,
      @NotNull MetaPointer containment,
      int oldIndex) {
    super(sequenceNumber);
    Objects.requireNonNull(movedChild, "movedChild should not be null");
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(containment, "containment should not be null");
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
    this.indexOffset = indexOffset;
    this.movedChild = movedChild;
    this.parent = parent;
    this.containment = containment;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "ChildMovedInSameContainment{"
        + "indexOffset="
        + indexOffset
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
