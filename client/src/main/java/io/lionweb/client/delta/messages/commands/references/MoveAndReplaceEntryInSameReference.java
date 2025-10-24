package io.lionweb.client.delta.messages.commands.references;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move existing entry movedTarget/movedResolveInfo[32] inside parent's reference at oldIndex inside
 * parent's reference at newIndex, replacing existing entry replacedTarget/replacedResolveInfo[32]
 * in parent's reference at newIndex.
 */
public final class MoveAndReplaceEntryInSameReference extends DeltaCommand {
  public final String parent;
  public final MetaPointer reference;
  public final int oldIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;
  public final int newIndex;
  public final @Nullable String replacedTarget;
  public final @Nullable String replacedResolveInfo;

  public MoveAndReplaceEntryInSameReference(
      @NotNull String commandId,
      String parent,
      MetaPointer reference,
      int oldIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo,
      int newIndex,
      @Nullable String replacedTarget,
      @Nullable String replacedResolveInfo) {
    super(commandId);
    this.parent = parent;
    this.reference = reference;
    this.oldIndex = oldIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
    this.newIndex = newIndex;
    this.replacedTarget = replacedTarget;
    this.replacedResolveInfo = replacedResolveInfo;
  }
}
