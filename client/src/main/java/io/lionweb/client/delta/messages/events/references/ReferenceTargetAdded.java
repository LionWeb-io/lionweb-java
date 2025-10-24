package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

/**
 * newTarget has been added as target to existing entry inside parent's reference at index with
 * resolveInfo.
 */
public class ReferenceTargetAdded extends CommonDeltaEvent {
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
}
