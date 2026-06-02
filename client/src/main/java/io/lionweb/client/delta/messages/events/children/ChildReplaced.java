package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildReplaced extends BaseDeltaEvent<ChildReplaced> {
  /** The parent node containing the replaced child. */
  public final @NotNull String parent;

  /** The new child node that replaces the old one. */
  public final @NotNull SerializationChunk newChild;

  /** The ID of the child node that was replaced. */
  public final @NotNull String replacedChild;

  /** The IDs of the descendants of the replaced child that were also removed. */
  public final @NotNull List<String> replacedDescendants;

  public final @NotNull MetaPointer containment;
  public final int index;

  public ChildReplaced(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull SerializationChunk newChild,
      @NotNull String replacedChild,
      @NotNull List<String> replacedDescendants,
      @NotNull MetaPointer containment,
      int index) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(newChild, "newChild should not be null");
    Objects.requireNonNull(replacedChild, "replacedChild should not be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants should not be null");
    Objects.requireNonNull(containment, "containment should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index should be non-negative");
    }
    this.parent = parent;
    this.newChild = newChild;
    this.replacedChild = replacedChild;
    this.replacedDescendants = replacedDescendants;
    this.containment = containment;
    this.index = index;
  }

  @Override
  public String toString() {
    return "ChildReplaced{"
        + "parent='"
        + parent
        + '\''
        + ", newChild="
        + newChild
        + ", replacedChild='"
        + replacedChild
        + '\''
        + ", replacedDescendants="
        + replacedDescendants
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
