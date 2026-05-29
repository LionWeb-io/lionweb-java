package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference with oldTarget/oldResolveInfo inside parent's reference at index has been
 * replaced with newTarget/newResolveInfo.
 */
public class ReferenceChanged extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;

  /** New target node id.*/
  public final @Nullable String newReference;

  public final @Nullable String newResolveInfo;

  /** Old target node id.*/
  public final @Nullable String oldReference;

  public final @Nullable String oldResolveInfo;

  public ReferenceChanged(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int index,
      @Nullable String newReference,
      @Nullable String newResolveInfo,
      @Nullable String oldReference,
      @Nullable String oldResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newReference = newReference;
    this.newResolveInfo = newResolveInfo;
    this.oldReference = oldReference;
    this.oldResolveInfo = oldResolveInfo;
  }

  @Override
  public String toString() {
    return "ReferenceChanged{"
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
        + ", oldReference='"
        + oldReference
        + '\''
        + ", oldResolveInfo='"
        + oldResolveInfo
        + '\''
        + '}';
  }
}
