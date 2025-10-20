package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;

/** Change oldResolveInfo of existing entry inside parent's reference at index to newResolveInfo. */
public final class ChangeReferenceResolveInfo extends DeltaCommand {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String oldResolveInfo;
  public final String newResolveInfo;

  public ChangeReferenceResolveInfo(
      @NotNull String commandId,
      String parent,
      MetaPointer reference,
      int index,
      String oldResolveInfo,
      String newResolveInfo) {
    super(commandId);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.oldResolveInfo = oldResolveInfo;
    this.newResolveInfo = newResolveInfo;
  }
}
