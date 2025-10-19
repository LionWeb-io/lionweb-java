package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;

/** Add newTarget as target to existing entry inside parent's reference at index. */
public final class AddReferenceTarget extends DeltaCommand {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String newTarget;

  public AddReferenceTarget(
      @NotNull String commandId,
      String parent,
      MetaPointer reference,
      int index,
      String newTarget) {
    super(commandId);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newTarget = newTarget;
  }
}
