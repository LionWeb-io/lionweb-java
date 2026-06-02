package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Target deletedTarget has been deleted from existing entry inside parent's reference at index with
 * resolveInfo.
 */
public class ReferenceTargetDeleted extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String resolveInfo;
  public final String deletedTarget;

  public ReferenceTargetDeleted(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String resolveInfo,
      @NotNull String deletedTarget) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent cannot be null");
    Objects.requireNonNull(reference, "reference cannot be null");
    Objects.requireNonNull(deletedTarget, "deletedTarget cannot be null");
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.resolveInfo = resolveInfo;
    this.deletedTarget = deletedTarget;
  }

  @Override
  public String toString() {
    return "ReferenceTargetDeleted{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", resolveInfo='"
        + resolveInfo
        + '\''
        + ", deletedTarget='"
        + deletedTarget
        + '\''
        + '}';
  }
}
