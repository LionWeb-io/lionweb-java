package io.lionweb.client.delta.messages.commands;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move existing entry movedTarget/movedResolveInfo inside oldParent's oldReference at oldIndex to
 * newParent's newReference at newIndex.
 */
public final class MoveEntryFromOtherReference extends DeltaCommand {
  public final String newParent;
  public final MetaPointer newReference;
  public final int newIndex;
  public final @NotNull String oldParent;
  public final MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public MoveEntryFromOtherReference(
      @NotNull String commandId,
      String newParent,
      MetaPointer newReference,
      int newIndex,
      @NotNull String oldParent,
      MetaPointer oldReference,
      int oldIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(commandId);
    this.newParent = newParent;
    this.newReference = newReference;
    this.newIndex = newIndex;
    this.oldParent = oldParent;
    this.oldReference = oldReference;
    this.oldIndex = oldIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
  }
}
