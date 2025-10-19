package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;

/**
 * Delete existing node deletedChild from parent's containment at index, and all its descendants
 * (including annotation instances). Does NOT change references to any of the deleted nodes.
 */
public class DeleteChild extends DeltaCommand {
  public String parent;
  public MetaPointer containment;
  public int index;
  public String deletedChild;

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
