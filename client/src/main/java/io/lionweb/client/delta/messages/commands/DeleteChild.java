package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;

/**
 * Delete existing node deletedChild from parent's containment at index, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes.
 */
public final class DeleteChild extends DeltaCommand {
  public final String parent;
  public final MetaPointer containment;
  public final int index;
  public final String deletedChild;

  public DeleteChild(
      @NotNull String commandId,
      String parent,
      MetaPointer containment,
      int index,
      String deletedChild) {
    super(commandId);
    this.parent = parent;
    this.containment = containment;
    this.index = index;
    this.deletedChild = deletedChild;
  }
}
