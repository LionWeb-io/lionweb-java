package io.lionweb.client.delta.messages.commands.annotations;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Add new node newAnnotation to parent's annotations at index. newAnnotation might be a single node
 * or an arbitrary complex subtree. All nodes in that subtree MUST be new, i.e. their id MUST NOT
 * exist in the repository. Nodes in that subtree MAY have references to already existing nodes, and
 * already existing nodes MAY have references to nodes in that subtree.
 */
public final class AddAnnotation extends DeltaCommand {
  public final @NotNull String parent;
  public final @NotNull SerializationChunk newAnnotation;
  public final int index;

  public AddAnnotation(
      @NotNull String commandId,
      @NotNull String parent,
      @NotNull SerializationChunk newAnnotation,
      int index) {
    super(commandId);
    Objects.requireNonNull(parent, "parent must not be null");
    Objects.requireNonNull(newAnnotation, "newAnnotation must not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index must be non-negative");
    }
    this.parent = parent;
    this.newAnnotation = newAnnotation;
    this.index = index;
  }

  @Override
  public String toString() {
    return "AddAnnotation{"
        + "parent='"
        + parent
        + '\''
        + ", newAnnotation="
        + newAnnotation
        + ", index="
        + index
        + ", commandId='"
        + commandId
        + '\''
        + ", protocolMessages="
        + protocolMessages
        + '}';
  }
}
