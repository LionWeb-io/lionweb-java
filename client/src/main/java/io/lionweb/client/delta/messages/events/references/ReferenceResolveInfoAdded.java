package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

/**
 * newResolveInfo has been added as ResolveInfo to existing entry inside parent's reference at index
 * with target.
 */
public class ReferenceResolveInfoAdded extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String newResolveInfo;
  public final String target;

  public ReferenceResolveInfoAdded(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int index,
      String newResolveInfo,
      String target) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newResolveInfo = newResolveInfo;
    this.target = target;
  }

  @Override
  public String toString() {
    return "ReferenceResolveInfoAdded{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", newResolveInfo='"
        + newResolveInfo
        + '\''
        + ", target='"
        + target
        + '\''
        + '}';
  }
}
