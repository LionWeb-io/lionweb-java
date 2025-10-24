package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import org.jetbrains.annotations.NotNull;

/**
 * Add new node newChild to parent in containment at index. newChild might be a single node or an
 * arbitrary complex subtree. All nodes in that subtree MUST be new, i.e. their id MUST NOT exist in
 * the repository. Nodes in that subtree MAY have references to already existing nodes, and already
 * existing nodes MAY have references to nodes in that subtree.
 */
public final class AddChild extends DeltaCommand {
  public final String parent;
  public final SerializationChunk newChild;
  public final MetaPointer containment;
  public final int index;

  public AddChild(
      @NotNull String commandId,
      String parent,
      SerializationChunk newChild,
      MetaPointer containment,
      int index) {
    super(commandId);
    this.parent = parent;
    this.newChild = newChild;
    this.containment = containment;
    this.index = index;
  }
}
