package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference movedTarget/movedResolveInfo (previously inside oldParent's oldReference at
 * oldIndex) has been moved to newParent's newReference at indexOffset.
 */
public class EntryMovedFromOtherReference extends BaseDeltaEvent<EntryMovedFromOtherReference> {
  public final @NotNull String newParent;
  public final @NotNull MetaPointer newReference;
  public final int indexOffset;
  public final @NotNull String oldParent;
  public final @NotNull MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public EntryMovedFromOtherReference(
      int sequenceNumber,
      @NotNull String newParent,
      @NotNull MetaPointer newReference,
      int indexOffset,
      @NotNull String oldParent,
      @NotNull MetaPointer oldReference,
      int oldIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(sequenceNumber);
    Objects.requireNonNull(newParent, "newParent should not be null");
    Objects.requireNonNull(newReference, "newReference should not be null");
    Objects.requireNonNull(oldParent, "oldParent should not be null");
    Objects.requireNonNull(oldReference, "oldReference should not be null");
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset should be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
    this.newParent = newParent;
    this.newReference = newReference;
    this.indexOffset = indexOffset;
    this.oldParent = oldParent;
    this.oldReference = oldReference;
    this.oldIndex = oldIndex;
    this.movedTarget = movedTarget;
    this.movedResolveInfo = movedResolveInfo;
  }

  @Override
  public String toString() {
    return "EntryMovedFromOtherReference{"
        + "newParent='"
        + newParent
        + '\''
        + ", newReference="
        + newReference
        + ", indexOffset="
        + indexOffset
        + ", oldParent='"
        + oldParent
        + '\''
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
