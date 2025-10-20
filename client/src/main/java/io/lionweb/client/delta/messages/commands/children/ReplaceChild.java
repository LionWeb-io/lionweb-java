package io.lionweb.client.delta.messages.commands.children;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import org.jetbrains.annotations.NotNull;

/**
 * Delete current child replacedChild inside parent's containment at index, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes
 */
public final class ReplaceChild extends DeltaCommand {
  public final SerializationChunk newChild;
  public final String parent;
  public final MetaPointer containment;
  public final int index;
  public final String replacedChild;

  public ReplaceChild(
      @NotNull String commandId,
      SerializationChunk newChild,
      String parent,
      MetaPointer containment,
      int index,
      String replacedChild) {
    super(commandId);
    this.newChild = newChild;
    this.parent = parent;
    this.containment = containment;
    this.index = index;
    this.replacedChild = replacedChild;
  }
}
