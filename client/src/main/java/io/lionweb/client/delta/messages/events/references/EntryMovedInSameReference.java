package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference movedTarget/movedResolveInfo (previously inside parent's reference at
 * oldIndex) has been moved to parent's reference at newIndex.
 */
public class EntryMovedInSameReference extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int oldIndex;
  public final int newIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public EntryMovedInSameReference(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int oldIndex,
      int newIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.oldIndex = oldIndex;
    this.newIndex = newIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
  }

  @Override
  public String toString() {
    return "EntryMovedInSameReference{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", oldIndex="
        + oldIndex
        + ", newIndex="
        + newIndex
        + ", movedTarget='"
        + movedTarget
        + '\''
        + ", movedResolveInfo='"
        + movedResolveInfo
        + '\''
        + '}';
  }
}
