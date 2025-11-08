package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference movedTarget/movedResolveInfo (previously inside parent's reference at
 * oldIndex) has replaced existing replacedTarget/replacedResolveInfo at parent's reference at
 * newIndex.
 */
public class EntryMovedAndReplacedInSameReference extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int newIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;
  public final int oldIndex;
  public final @Nullable String replacedTarget;
  public final @Nullable String replacedResolveInfo;

  public EntryMovedAndReplacedInSameReference(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int newIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo,
      int oldIndex,
      @Nullable String replacedTarget,
      @Nullable String replacedResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.newIndex = newIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
    this.oldIndex = oldIndex;
    this.replacedTarget = replacedTarget;
    this.replacedResolveInfo = replacedResolveInfo;
  }

  @Override
  public String toString() {
    return "EntryMovedAndReplacedInSameReference{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", newIndex="
        + newIndex
        + ", movedTarget='"
        + movedTarget
        + '\''
        + ", movedResolveInfo='"
        + movedResolveInfo
        + '\''
        + ", oldIndex="
        + oldIndex
        + ", replacedTarget='"
        + replacedTarget
        + '\''
        + ", replacedResolveInfo='"
        + replacedResolveInfo
        + '\''
        + '}';
  }
}
