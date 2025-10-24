package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move existing entry movedTarget/movedResolveInfo inside parent's oldReference at oldIndex to
 * parent's newReference at newIndex.
 */
public final class MoveEntryFromOtherReferenceInSameParent extends DeltaCommand {
  public final String parent;
  public final MetaPointer newReference;
  public final int newIndex;
  public final MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public MoveEntryFromOtherReferenceInSameParent(
      @NotNull String commandId,
      String parent,
      MetaPointer newReference,
      int newIndex,
      MetaPointer oldReference,
      int oldIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(commandId);
    this.parent = parent;
    this.newReference = newReference;
    this.newIndex = newIndex;
    this.oldReference = oldReference;
    this.oldIndex = oldIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
  }
}
