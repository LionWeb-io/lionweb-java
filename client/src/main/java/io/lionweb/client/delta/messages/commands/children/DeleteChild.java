package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Delete existing node deletedChild from parent's containment at index, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes.
 */
public final class DeleteChild extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull MetaPointer containment;
  public final int index;
  public final @NotNull String deletedChild;

  public DeleteChild(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull MetaPointer containment,
      int index,
      @NotNull String deletedChild) {
    super(commandId);
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(containment, "containment must not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index must be non-negative");
    }
    Objects.requireNonNull(deletedChild, "deletedChild must not be null");
    this.parent = parent;
    this.containment = containment;
    this.index = index;
    this.deletedChild = deletedChild;
  }

  @Override
  public String toString() {
    return "DeleteChild{"
        + "parent='"
        + parent
        + '\''
        + ", containment="
        + containment
        + ", index="
        + index
        + ", deletedChild='"
        + deletedChild
        + '\''
        + ", commandId='"
        + commandId
        + '\''
        + ", protocolMessages="
        + protocolMessages
        + '}';
  }
}
