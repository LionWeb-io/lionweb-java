package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildMovedAndReplacedFromOtherContainmentInSameParent
    extends BaseDeltaEvent<ChildMovedAndReplacedFromOtherContainmentInSameParent> {
  public final @NotNull MetaPointer newContainment;
  public final int indexOffset;
  public final @NotNull String movedChild;
  public final @NotNull String parent;
  public final @NotNull MetaPointer oldContainment;
  public final int oldIndex;
  public final @NotNull String replacedChild;
  public final @NotNull List<String> replacedDescendants;

  public ChildMovedAndReplacedFromOtherContainmentInSameParent(
      int sequenceNumber,
      @NotNull MetaPointer newContainment,
      int indexOffset,
      @NotNull String movedChild,
      @NotNull String parent,
      @NotNull MetaPointer oldContainment,
      int oldIndex,
      @NotNull String replacedChild,
      @NotNull List<String> replacedDescendants) {
    super(sequenceNumber);
    Objects.requireNonNull(newContainment, "newContainment should not be null");
    Objects.requireNonNull(movedChild, "movedChild should not be null");
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(oldContainment, "oldContainment should not be null");
    Objects.requireNonNull(replacedChild, "replacedChild should not be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants should not be null");
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset should be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
    this.newContainment = newContainment;
    this.indexOffset = indexOffset;
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
        + ", indexOffset="
        + indexOffset
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
