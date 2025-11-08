package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference movedTarget/movedResolveInfo (previously inside parent's oldReference at
 * oldIndex) has been moved to parent's newReference at newIndex.
 */
public class EntryMovedFromOtherReferenceInSameParent extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer newReference;
  public final int newIndex;
  public final MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public EntryMovedFromOtherReferenceInSameParent(
      int sequenceNumber,
      String parent,
      MetaPointer newReference,
      int newIndex,
      MetaPointer oldReference,
      int oldIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.newReference = newReference;
    this.newIndex = newIndex;
    this.oldReference = oldReference;
    this.oldIndex = oldIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
  }

  @Override
  public String toString() {
    return "EntryMovedFromOtherReferenceInSameParent{"
        + "parent='"
        + parent
        + '\''
        + ", newReference="
        + newReference
        + ", newIndex="
        + newIndex
        + ", oldReference="
        + oldReference
        + ", oldIndex="
        + oldIndex
        + ", movedTarget='"
        + movedTarget
        + '\''
        + ", movedResolveInfo='"
        + movedResolveInfo
        + '\''
        + '}';
  }
}
