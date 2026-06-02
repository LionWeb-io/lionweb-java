package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference movedTarget/movedResolveInfo (previously inside oldParent's oldReference at
 * oldIndex) has replaced existing replacedTarget/replacedResolveInfo at newParent's newReference at
 * newIndex.
 */
public class EntryMovedAndReplacedFromOtherReference
    extends BaseDeltaEvent<EntryMovedAndReplacedFromOtherReference> {
  public final @NotNull String newParent;
  public final @NotNull MetaPointer newReference;
  public final int newIndex;
  public final @Nullable String movedTarget;
  public final @Nullable String movedResolveInfo;
  public final @NotNull String oldParent;
  public final @NotNull MetaPointer oldReference;
  public final int oldIndex;
  public final @Nullable String replacedTarget;
  public final @Nullable String replacedResolveInfo;

  public EntryMovedAndReplacedFromOtherReference(
      int sequenceNumber,
      @NotNull String newParent,
      @NotNull MetaPointer newReference,
      int newIndex,
      @Nullable String movedTarget,
      @Nullable String movedResolveInfo,
      @NotNull String oldParent,
      @NotNull MetaPointer oldReference,
      int oldIndex,
      @Nullable String replacedTarget,
      @Nullable String replacedResolveInfo) {
    super(sequenceNumber);
    Objects.requireNonNull(newParent, "newParent should not be null");
    Objects.requireNonNull(newReference, "newReference should not be null");
    Objects.requireNonNull(oldParent, "oldParent should not be null");
    Objects.requireNonNull(oldReference, "oldReference should not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex should be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
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
