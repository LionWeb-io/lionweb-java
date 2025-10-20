package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move existing entry movedTarget/movedResolveInfo inside parent's reference at oldIndex inside
 * parent's reference at newIndex.
 */
public final class MoveEntryInSameReference extends DeltaCommand {
  public final String parent;
  public final MetaPointer reference;
  public final int oldIndex;
  public final int newIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public MoveEntryInSameReference(
      @NotNull String commandId,
      String parent,
      MetaPointer reference,
      int oldIndex,
      int newIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(commandId);
    this.parent = parent;
    this.reference = reference;
    this.oldIndex = oldIndex;
    this.newIndex = newIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
  }
}
