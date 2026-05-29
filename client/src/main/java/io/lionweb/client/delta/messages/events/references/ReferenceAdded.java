package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/** Reference with newTarget/newResolveInfo has been added to parent's reference at index. */
public class ReferenceAdded extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;

  /** Target node id.*/
  public final @Nullable String newReference;

  public final @Nullable String newResolveInfo;

  public ReferenceAdded(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int index,
      @Nullable String newReference,
      @Nullable String newResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newReference = newReference;
    this.newResolveInfo = newResolveInfo;
  }

  @Override
  public String toString() {
    return "ReferenceAdded{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", newReference='"
        + newReference
        + '\''
        + ", newResolveInfo='"
        + newResolveInfo
        + '\''
        + '}';
  }
}
