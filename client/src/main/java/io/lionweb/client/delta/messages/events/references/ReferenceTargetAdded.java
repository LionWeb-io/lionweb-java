package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

/**
 * newTarget has been added as target to existing entry inside parent's reference at index with
 * resolveInfo.
 */
public class ReferenceTargetAdded extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String newTarget;
  public final String resolveInfo;

  public ReferenceTargetAdded(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int index,
      String newTarget,
      String resolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newTarget = newTarget;
    this.resolveInfo = resolveInfo;
  }

  @Override
  public String toString() {
    return "ReferenceTargetAdded{"
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
        + '}';
  }
}
