package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildMovedAndReplacedInSameContainment extends BaseDeltaEvent {
  public final int newIndex;
  public final String movedChild;
  public final String parent;
  public final MetaPointer containment;
  public final int oldIndex;
  public final String replacedChild;
  public final List<String> replacedDescendants;

  public ChildMovedAndReplacedInSameContainment(
      int sequenceNumber,
      int newIndex,
      @NotNull String movedChild,
      @NotNull String parent,
      @NotNull MetaPointer containment,
      int oldIndex,
      @NotNull String replacedChild,
      @NotNull List<String> replacedDescendants) {
    super(sequenceNumber);
    Objects.requireNonNull(movedChild, "movedChild cannot be null");
    Objects.requireNonNull(parent, "parent cannot be null");
    Objects.requireNonNull(containment, "containment cannot be null");
    Objects.requireNonNull(replacedChild, "replacedChild cannot be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants cannot be null");
    this.newIndex = newIndex;
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
        + ", replacedChild='"
        + replacedChild
        + '\''
        + ", replacedDescendants="
        + replacedDescendants
        + '}';
  }
}
