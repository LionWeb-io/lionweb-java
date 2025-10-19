package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Replace existing entry oldTarget/oldResolveInfo inside parent's reference at index with
 * newTarget/newResolveInfo.
 */
public final class ChangeReference extends DeltaCommand {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final @Nullable String oldTarget;
  public final @Nullable String oldResolveInfo;
  public final @Nullable String newTarget;
  public final @Nullable String newResolveInfo;

  public ChangeReference(
      @NotNull String commandId,
      String parent,
      MetaPointer reference,
      int index,
      @Nullable String oldTarget,
      @Nullable String oldResolveInfo,
      @Nullable String newTarget,
      @Nullable String newResolveInfo) {
    super(commandId);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.oldTarget = oldTarget;
    this.oldResolveInfo = oldResolveInfo;
    this.newTarget = newTarget;
    this.newResolveInfo = newResolveInfo;
  }
}
