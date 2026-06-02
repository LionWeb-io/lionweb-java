package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildMovedAndReplacedFromOtherContainmentInSameParent extends BaseDeltaEvent {
  public final MetaPointer newContainment;
  public final int newIndex;
  public final String movedChild;
  public final String parent;
  public final MetaPointer oldContainment;
  public final int oldIndex;
  public final String replacedChild;
  public final List<String> replacedDescendants;

  public ChildMovedAndReplacedFromOtherContainmentInSameParent(
      int sequenceNumber,
      @NotNull MetaPointer newContainment,
      int newIndex,
      @NotNull String movedChild,
      @NotNull String parent,
      @NotNull MetaPointer oldContainment,
      int oldIndex,
      @NotNull String replacedChild,
      @NotNull List<String> replacedDescendants) {
    super(sequenceNumber);
    Objects.requireNonNull(newContainment, "newContainment cannot be null");
    Objects.requireNonNull(movedChild, "movedChild cannot be null");
    Objects.requireNonNull(parent, "parent cannot be null");
    Objects.requireNonNull(oldContainment, "oldContainment cannot be null");
    Objects.requireNonNull(replacedChild, "replacedChild cannot be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants cannot be null");
    this.newContainment = newContainment;
    this.newIndex = newIndex;
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
        + ", newIndex="
        + newIndex
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
