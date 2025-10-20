package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference movedTarget/movedResolveInfo (previously inside parent's oldReference at
 * oldIndex) has replaced existing replacedTarget/replacedResolveInfo at parent's newReference at
 * newIndex.
 */
public class EntryMovedAndReplacedFromOtherReferenceInSameParent extends CommonDeltaEvent {
  public final String parent;
  public final MetaPointer newReference;
  public final int newIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;
  public final MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String replacedTarget;
  public final @Nullable String replacedResolveInfo;

  public EntryMovedAndReplacedFromOtherReferenceInSameParent(
      int sequenceNumber,
      String parent,
      MetaPointer newReference,
      int newIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo,
      MetaPointer oldReference,
      int oldIndex,
      @Nullable String replacedTarget,
      @Nullable String replacedResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.newReference = newReference;
    this.newIndex = newIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
    this.oldReference = oldReference;
    this.oldIndex = oldIndex;
    this.replacedTarget = replacedTarget;
    this.replacedResolveInfo = replacedResolveInfo;
  }
}
