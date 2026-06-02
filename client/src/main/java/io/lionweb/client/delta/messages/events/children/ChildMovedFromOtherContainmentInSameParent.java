package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildMovedFromOtherContainmentInSameParent extends BaseDeltaEvent {
  /** The new containment link after the move. */
  public final MetaPointer newContainment;

  /** The new index in the containment after the move. */
  public final int newIndex;

  /** The ID of the child node that was moved. */
  public final String movedChild;

  /** The parent node (same before and after the move). */
  public String parent;

  /** The old containment link before the move. */
  public MetaPointer oldContainment;

  /** The old index in the containment before the move. */
  public int oldIndex;

  public ChildMovedFromOtherContainmentInSameParent(
      int sequenceNumber,
      @NotNull MetaPointer newContainment,
      int newIndex,
      @NotNull String movedChild) {
    super(sequenceNumber);
    Objects.requireNonNull(newContainment, "newContainment cannot be null");
    Objects.requireNonNull(movedChild, "movedChild cannot be null");
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.movedChild = movedChild;
  }

  @Override
  public String toString() {
    return "ChildMovedFromOtherContainmentInSameParent{"
        + "newContainment="
        + newContainment
        + ", newIndex="
        + newIndex
        + ", movedChild='"
        + movedChild
        + '\''
        + '}';
  }
}
