package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class ChildDeleted extends BaseDeltaEvent {
  /** The parent node whose child was deleted. */
  public final @NotNull String parent;

  /** The ID of the child node that was deleted. */
  public final @NotNull String deletedChild;

  /** The IDs of all descendants of the deleted child. */
  public final @NotNull List<String> deletedDescendants;

  public final @NotNull MetaPointer containment;
  public final int index;

  public ChildDeleted(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull String deletedChild,
      @NotNull List<String> deletedDescendants,
      int index,
      @NotNull MetaPointer containment) {
    super(sequenceNumber);
    this.parent = parent;
    this.deletedChild = deletedChild;
    this.deletedDescendants = deletedDescendants;
    this.index = index;
    this.containment = containment;
  }

  @Override
  public String toString() {
    return "ChildDeleted{"
        + "parent='"
        + parent
        + '\''
        + ", deletedChild='"
        + deletedChild
        + '\''
        + ", deletedDescendants="
        + deletedDescendants
        + ", containment="
        + containment
        + ", index="
        + index
        + ", sequenceNumber="
        + sequenceNumber
        + ", originCommands="
        + originCommands
        + ", split="
        + split
        + ", additionalInfos="
        + additionalInfos
        + '}';
  }
}
