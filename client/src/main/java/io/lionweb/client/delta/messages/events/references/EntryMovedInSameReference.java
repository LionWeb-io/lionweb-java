package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference movedTarget/movedResolveInfo (previously inside parent's reference at
 * oldIndex) has been moved to parent's reference at indexOffset.
 */
public class EntryMovedInSameReference extends BaseDeltaEvent<EntryMovedInSameReference> {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int oldIndex;
  public final int indexOffset;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public EntryMovedInSameReference(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int oldIndex,
      int indexOffset,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(reference, "reference should not be null");
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset should be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
    this.parent = parent;
    this.reference = reference;
    this.oldIndex = oldIndex;
    this.indexOffset = indexOffset;
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
        + ", indexOffset="
        + indexOffset
        + ", movedTarget='"
        + movedTarget
        + '\''
        + ", movedResolveInfo='"
        + movedResolveInfo
        + '\''
        + '}';
  }
}
