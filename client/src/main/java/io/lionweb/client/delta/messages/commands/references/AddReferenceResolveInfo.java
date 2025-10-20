package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;

/** Add newResolveInfo as ResolveInfo to existing entry inside parent's reference at index. */
public final class AddReferenceResolveInfo extends DeltaCommand {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String newResolveInfo;

  public AddReferenceResolveInfo(
      @NotNull String commandId,
      String parent,
      MetaPointer reference,
      int index,
      String newResolveInfo) {
    super(commandId);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newResolveInfo = newResolveInfo;
  }
}
