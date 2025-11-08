package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

/**
 * ResolveInfo deletedResolveInfo has been deleted from existing entry inside parent's reference at
 * index with target.
 */
public class ReferenceResolveInfoDeleted extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String target;
  public final String deletedResolveInfo;

  public ReferenceResolveInfoDeleted(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int index,
      String target,
      String deletedResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.target = target;
    this.deletedResolveInfo = deletedResolveInfo;
  }

  @Override
  public String toString() {
    return "ReferenceResolveInfoDeleted{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", target='"
        + target
        + '\''
        + ", deletedResolveInfo='"
        + deletedResolveInfo
        + '\''
        + '}';
  }
}
