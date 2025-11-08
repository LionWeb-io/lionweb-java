package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * Target of existing entry inside parent's reference at index with resolveInfo has been changed
 * from replacedTarget to newTarget.
 */
public class ReferenceTargetChanged extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String newTarget;
  public final @Nullable String resolveInfo;
  public final String replacedTarget;

  public ReferenceTargetChanged(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int index,
      String newTarget,
      @Nullable String resolveInfo,
      String replacedTarget) {
    super(sequenceNumber);
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
