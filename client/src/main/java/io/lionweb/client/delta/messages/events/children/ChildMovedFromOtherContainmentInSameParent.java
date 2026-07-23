package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChildMovedFromOtherContainmentInSameParent
    extends BaseDeltaEvent<ChildMovedFromOtherContainmentInSameParent> {
  /** The new containment link after the move. */
  public final @NotNull MetaPointer newContainment;

  /** The new index in the containment after the move. */
  public final int indexOffset;

  /** The ID of the child node that was moved. */
  public final @NotNull String movedChild;

  /** The parent node (same before and after the move). */
  public @Nullable String parent;

  /** The old containment link before the move. */
  public @Nullable MetaPointer oldContainment;

  /** The old index in the containment before the move. */
  public int oldIndex;

  public ChildMovedFromOtherContainmentInSameParent(
      int sequenceNumber,
      @NotNull MetaPointer newContainment,
      int indexOffset,
      @NotNull String movedChild) {
    super(sequenceNumber);
    Objects.requireNonNull(newContainment, "newContainment should not be null");
    Objects.requireNonNull(movedChild, "movedChild should not be null");
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset should be non-negative");
    }
    this.newContainment = newContainment;
    this.indexOffset = indexOffset;
    this.movedChild = movedChild;
  }

  @Override
  public String toString() {
    return "ChildMovedFromOtherContainmentInSameParent{"
        + "newContainment="
        + newContainment
        + ", indexOffset="
        + indexOffset
        + ", movedChild='"
        + movedChild
        + '\''
        + '}';
  }
}
