package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference movedTarget/movedResolveInfo (previously inside parent's reference at
 * oldIndex) has been moved to parent's reference at newIndex.
 */
public class EntryMovedInSameReference extends BaseDeltaEvent<EntryMovedInSameReference> {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int oldIndex;
  public final int newIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;

  public EntryMovedInSameReference(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int oldIndex,
      int newIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(reference, "reference should not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex should be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
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
