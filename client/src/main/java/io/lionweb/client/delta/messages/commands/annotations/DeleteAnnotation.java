package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Delete existing node deletedAnnotation from parent's annotations at index, and all its
 * descendants (including annotation instances). Does NOT change references to any of the deleted
 * nodes.
 */
public final class DeleteAnnotation extends DeltaCommand {
  public @NotNull final String node;
  public final int index;
  public @NotNull final String deletedAnnotation;

  public DeleteAnnotation(
      @NotNull String commandId,
      @NotNull String node,
      int index,
      @NotNull String deletedAnnotation) {
    super(commandId);
    Objects.requireNonNull(node, "node must not be null");
    Objects.requireNonNull(deletedAnnotation, "deletedAnnotation must not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index must be non-negative");
    }
    this.node = node;
    this.index = index;
    this.deletedAnnotation = deletedAnnotation;
  }

  @Override
  public String toString() {
    return "DeleteAnnotation{"
        + "node='"
        + node
        + '\''
        + ", index="
        + index
        + ", deletedAnnotation='"
        + deletedAnnotation
        + '\''
        + ", commandId='"
        + commandId
        + '\''
        + ", protocolMessages="
        + protocolMessages
        + '}';
  }
}
