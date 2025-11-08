package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Delete current child replacedChild inside parent's containment at index, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes
 */
public final class ReplaceChild extends DeltaCommand {
  public final @NotNull SerializationChunk newChild;
  public final @NotNull String parent;
  public final @NotNull MetaPointer containment;
  public final int index;
  public final @NotNull String replacedChild;

  public ReplaceChild(
      @NotNull String commandId,
      @NotNull SerializationChunk newChild,
      @NotNull String parent,
      @NotNull MetaPointer containment,
      int index,
      @NotNull String replacedChild) {
    super(commandId);
    Objects.requireNonNull(newChild, "newChild must not be null");
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(containment, "containment must not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index must be non-negative");
    }
    Objects.requireNonNull(replacedChild, "replacedChild must not be null");
    this.newChild = newChild;
    this.parent = parent;
    this.containment = containment;
    this.index = index;
    this.replacedChild = replacedChild;
  }

  @Override
  public String toString() {
    return "ReplaceChild{"
        + "newChild="
        + newChild
        + ", parent='"
        + parent
        + '\''
        + ", containment="
        + containment
        + ", index="
        + index
        + ", replacedChild='"
        + replacedChild
        + '\''
        + '}';
  }
}
