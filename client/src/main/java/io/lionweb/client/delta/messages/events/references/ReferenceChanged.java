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
  public final @Nullable String newTarget;
  public final @Nullable String newResolveInfo;
  public final @Nullable String oldTarget;
  public final @Nullable String oldResolveInfo;

  public ReferenceChanged(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int index,
      @Nullable String newTarget,
      @Nullable String newResolveInfo,
      @Nullable String oldTarget,
      @Nullable String oldResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newTarget = newTarget;
    this.newResolveInfo = newResolveInfo;
    this.oldTarget = oldTarget;
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
        + ", newTarget='"
        + newTarget
        + '\''
        + ", newResolveInfo='"
        + newResolveInfo
        + '\''
        + ", oldTarget='"
        + oldTarget
        + '\''
        + ", oldResolveInfo='"
        + oldResolveInfo
        + '\''
        + '}';
  }
}
