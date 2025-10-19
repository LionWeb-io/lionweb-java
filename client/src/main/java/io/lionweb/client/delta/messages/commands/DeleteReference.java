package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Delete existing entry deletedTarget/deletedResolveInfo from parent's reference at index. */
public class DeleteReference extends DeltaCommand {
  public String parent;
  public MetaPointer reference;
  public int index;
  public @Nullable String deletedTarget;
  public @Nullable String deletedResolveInfo;

  public DeleteReference(
      @NotNull String commandId,
      String parent,
      MetaPointer reference,
      int index,
      @Nullable String deletedTarget,
      @Nullable String deletedResolveInfo) {
    super(commandId);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.deletedTarget = deletedTarget;
    this.deletedResolveInfo = deletedResolveInfo;
  }
}
