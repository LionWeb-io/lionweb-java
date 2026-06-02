package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildMovedAndReplacedFromOtherContainment extends BaseDeltaEvent<ChildMovedAndReplacedFromOtherContainment> {
  /** The new parent node after the move. */
  public final @NotNull String newParent;

  /** The new containment link after the move. */
  public final @NotNull MetaPointer newContainment;

  /** The new index in the containment after the move. */
  public final int newIndex;

  /** The ID of the child node that was moved. */
  public final @NotNull String movedChild;

  /** The old parent node before the move. */
  public final @NotNull String oldParent;

  /** The old containment link before the move. */
  public final @NotNull MetaPointer oldContainment;

  /** The old index in the containment before the move. */
  public final int oldIndex;

  /** The ID of the child that was replaced. */
  public final @NotNull String replacedChild;

  /** The IDs of all descendants of the replaced child. */
  public final @NotNull List<String> replacedDescendants;

  public ChildMovedAndReplacedFromOtherContainment(
      int sequenceNumber,
      @NotNull String newParent,
      @NotNull MetaPointer newContainment,
      int newIndex,
      @NotNull String movedChild,
      @NotNull String oldParent,
      @NotNull MetaPointer oldContainment,
      int oldIndex,
      @NotNull String replacedChild,
      @NotNull List<String> replacedDescendants) {
    super(sequenceNumber);
    Objects.requireNonNull(newParent, "newParent should not be null");
    Objects.requireNonNull(newContainment, "newContainment should not be null");
    Objects.requireNonNull(movedChild, "movedChild should not be null");
    Objects.requireNonNull(oldParent, "oldParent should not be null");
    Objects.requireNonNull(oldContainment, "oldContainment should not be null");
    Objects.requireNonNull(replacedChild, "replacedChild should not be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants should not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex should be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
    this.newParent = newParent;
    this.newContainment = newContainment;
    this.newIndex = newIndex;
    this.movedChild = movedChild;
    this.oldParent = oldParent;
    this.oldContainment = oldContainment;
    this.oldIndex = oldIndex;
    this.replacedChild = replacedChild;
    this.replacedDescendants = replacedDescendants;
  }

  @Override
  public String toString() {
    return "ChildMovedAndReplacedFromOtherContainment{"
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
        + ", oldParent='"
        + oldParent
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
