package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;

/** Change oldTarget of existing entry inside parent's reference at index to newTarget. */
public final class ChangeReferenceTarget extends DeltaCommand {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String oldTarget;
  public final String newTarget;

  public ChangeReferenceTarget(
      @NotNull String commandId,
      String parent,
      MetaPointer reference,
      int index,
      String oldTarget,
      String newTarget) {
    super(commandId);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.oldTarget = oldTarget;
    this.newTarget = newTarget;
  }
}
