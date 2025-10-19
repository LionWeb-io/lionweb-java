package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Delete existing entry deletedTarget/deletedResolveInfo from parent's reference at index. */
public final class DeleteReference extends DeltaCommand {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final @Nullable String deletedTarget;
  public final @Nullable String deletedResolveInfo;

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
