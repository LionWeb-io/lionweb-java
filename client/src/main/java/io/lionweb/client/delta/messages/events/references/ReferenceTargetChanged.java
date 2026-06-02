package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Target of existing entry inside parent's reference at index with resolveInfo has been changed
 * from replacedTarget to newTarget.
 */
public class ReferenceTargetChanged extends BaseDeltaEvent<ReferenceTargetChanged> {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;
  public final @NotNull String newTarget;
  public final @Nullable String resolveInfo;
  public final @NotNull String replacedTarget;

  public ReferenceTargetChanged(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @NotNull String newTarget,
      @Nullable String resolveInfo,
      @NotNull String replacedTarget) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(reference, "reference should not be null");
    Objects.requireNonNull(newTarget, "newTarget should not be null");
    Objects.requireNonNull(replacedTarget, "replacedTarget should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index should be non-negative");
    }
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newTarget = newTarget;
    this.resolveInfo = resolveInfo;
    this.replacedTarget = replacedTarget;
  }

  @Override
  public String toString() {
    return "ReferenceTargetChanged{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", newTarget='"
        + newTarget
        + '\''
        + ", resolveInfo='"
        + resolveInfo
        + '\''
        + ", replacedTarget='"
        + replacedTarget
        + '\''
        + '}';
  }
}
