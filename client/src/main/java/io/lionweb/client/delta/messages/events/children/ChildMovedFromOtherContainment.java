package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChildMovedFromOtherContainment
    extends BaseDeltaEvent<ChildMovedFromOtherContainment> {
  /** The new parent node after the move. */
  public final @NotNull String newParent;

  /** The new containment link after the move. */
  public final @NotNull MetaPointer newContainment;

  /** The new index in the containment after the move. */
  public final int indexOffset;

  /** The ID of the child node that was moved. */
  public final @NotNull String movedChild;

  /** The old parent node before the move. */
  public @Nullable String oldParent;

  /** The old containment link before the move. */
  public @Nullable MetaPointer oldContainment;

  /** The old index in the containment before the move. */
  public int oldIndex;

  public ChildMovedFromOtherContainment(
      int sequenceNumber,
      @NotNull String newParent,
      @NotNull MetaPointer newContainment,
      int indexOffset,
      @NotNull String movedChild) {
    super(sequenceNumber);
    Objects.requireNonNull(newParent, "newParent should not be null");
    Objects.requireNonNull(newContainment, "newContainment should not be null");
    Objects.requireNonNull(movedChild, "movedChild should not be null");
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset should be non-negative");
    }
    this.newParent = newParent;
    this.newContainment = newContainment;
    this.indexOffset = indexOffset;
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
        + ", indexOffset="
        + indexOffset
        + ", movedChild='"
        + movedChild
        + '\''
        + '}';
  }
}
