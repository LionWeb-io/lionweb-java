package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference movedTarget/movedResolveInfo (previously inside oldParent's oldReference at
 * oldIndex) has replaced existing replacedTarget/replacedResolveInfo at newParent's newReference at
 * newIndex.
 */
public class EntryMovedAndReplacedFromOtherReference extends BaseDeltaEvent {
  public final String newParent;
  public final MetaPointer newReference;
  public final int newIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;
  public final String oldParent;
  public final MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String replacedTarget;
  public final @Nullable String replacedResolveInfo;

  public EntryMovedAndReplacedFromOtherReference(
      int sequenceNumber,
      String newParent,
      MetaPointer newReference,
      int newIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo,
      String oldParent,
      MetaPointer oldReference,
      int oldIndex,
      @Nullable String replacedTarget,
      @Nullable String replacedResolveInfo) {
    super(sequenceNumber);
    this.newParent = newParent;
    this.newReference = newReference;
    this.newIndex = newIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
    this.oldParent = oldParent;
    this.oldReference = oldReference;
    this.oldIndex = oldIndex;
    this.replacedTarget = replacedTarget;
    this.replacedResolveInfo = replacedResolveInfo;
  }

  @Override
  public String toString() {
    return "EntryMovedAndReplacedFromOtherReference{"
        + "newParent='"
        + newParent
        + '\''
        + ", newReference="
        + newReference
        + ", newIndex="
        + newIndex
        + ", movedTarget='"
        + movedTarget
        + '\''
        + ", movedResolveInfo='"
        + movedResolveInfo
        + '\''
        + ", oldParent='"
        + oldParent
        + '\''
        + ", oldReference="
        + oldReference
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
