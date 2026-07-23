package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildMovedAndReplacedInSameContainment
    extends BaseDeltaEvent<ChildMovedAndReplacedInSameContainment> {
  public final int indexOffset;
  public final @NotNull String movedChild;
  public final @NotNull String parent;
  public final @NotNull MetaPointer containment;
  public final int oldIndex;
  public final @NotNull String replacedChild;
  public final @NotNull List<String> replacedDescendants;

  public ChildMovedAndReplacedInSameContainment(
      int sequenceNumber,
      int indexOffset,
      @NotNull String movedChild,
      @NotNull String parent,
      @NotNull MetaPointer containment,
      int oldIndex,
      @NotNull String replacedChild,
      @NotNull List<String> replacedDescendants) {
    super(sequenceNumber);
    Objects.requireNonNull(movedChild, "movedChild should not be null");
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(containment, "containment should not be null");
    Objects.requireNonNull(replacedChild, "replacedChild should not be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants should not be null");
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
    this.indexOffset = indexOffset;
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
        + ", replacedChild='"
        + replacedChild
        + '\''
        + ", replacedDescendants="
        + replacedDescendants
        + '}';
  }
}
