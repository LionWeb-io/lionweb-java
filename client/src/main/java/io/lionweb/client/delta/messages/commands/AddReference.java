package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Add newTarget / newResolveInfo to parent's reference at index. */
public class AddReference extends DeltaCommand {
  public String parent;
  public MetaPointer reference;
  public int index;
  public @Nullable String newTarget;
  public @Nullable String newResolveInfo;

  public AddReference(
      @NotNull String commandId,
      String parent,
      MetaPointer reference,
      int index,
      @Nullable String newTarget,
      @Nullable String newResolveInfo) {
    super(commandId);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newTarget = newTarget;
    this.newResolveInfo = newResolveInfo;
  }
}
