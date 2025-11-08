package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * ResolveInfo of existing entry inside parent's reference at index with target has been changed
 * from oldResolveInfo to newResolveInfo.
 */
public class ReferenceResolveInfoChanged extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String newResolveInfo;
  public final @Nullable String target;
  public final String oldResolveInfo;

  public ReferenceResolveInfoChanged(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int index,
      String newResolveInfo,
      @Nullable String target,
      String oldResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newResolveInfo = newResolveInfo;
    this.target = target;
    this.oldResolveInfo = oldResolveInfo;
  }

  @Override
  public String toString() {
    return "ReferenceResolveInfoChanged{"
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
        + ", oldResolveInfo='"
        + oldResolveInfo
        + '\''
        + '}';
  }
}
