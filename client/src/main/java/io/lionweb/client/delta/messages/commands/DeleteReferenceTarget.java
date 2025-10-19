package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;

/** Delete existing deletedTarget from existing entry inside parent's reference at index. */
public final class DeleteReferenceTarget extends DeltaCommand {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String deletedTarget;

  public DeleteReferenceTarget(
      @NotNull String commandId,
      String parent,
      MetaPointer reference,
      int index,
      String deletedTarget) {
    super(commandId);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.deletedTarget = deletedTarget;
  }
}
