package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference movedTarget/movedResolveInfo (previously inside oldParent's oldReference at
 * oldIndex) has been moved to newParent's newReference at newIndex.
 */
public class EntryMovedFromOtherReference extends CommonDeltaEvent {
  public final String newParent;
  public final MetaPointer newReference;
  public final int newIndex;
  public final String oldParent;
  public final MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public EntryMovedFromOtherReference(
      int sequenceNumber,
      String newParent,
      MetaPointer newReference,
      int newIndex,
      String oldParent,
      MetaPointer oldReference,
      int oldIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(sequenceNumber);
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
