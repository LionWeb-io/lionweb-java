package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildMovedFromOtherContainment extends BaseDeltaEvent {
  /** The new parent node after the move. */
  public final String newParent;

  /** The new containment link after the move. */
  public final MetaPointer newContainment;

  /** The new index in the containment after the move. */
  public final int newIndex;

  /** The ID of the child node that was moved. */
  public final String movedChild;

  /** The old parent node before the move. */
  public String oldParent;

  /** The old containment link before the move. */
  public MetaPointer oldContainment;

  /** The old index in the containment before the move. */
  public int oldIndex;

  public ChildMovedFromOtherContainment(
      int sequenceNumber,
      @NotNull String newParent,
      @NotNull MetaPointer newContainment,
      int newIndex,
      @NotNull String movedChild) {
    super(sequenceNumber);
    Objects.requireNonNull(newParent, "newParent cannot be null");
    Objects.requireNonNull(newContainment, "newContainment cannot be null");
    Objects.requireNonNull(movedChild, "movedChild cannot be null");
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
