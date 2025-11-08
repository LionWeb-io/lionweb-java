package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference with deletedTarget/deletedResolveInfo has been deleted from parent's reference
 * at index.
 */
public class ReferenceDeleted extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final @Nullable String deletedTarget;
  public final @Nullable String deletedResolveInfo;

  public ReferenceDeleted(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int index,
      @Nullable String deletedTarget,
      @Nullable String deletedResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.deletedTarget = deletedTarget;
    this.deletedResolveInfo = deletedResolveInfo;
  }

  @Override
  public String toString() {
    return "ReferenceDeleted{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", deletedTarget='"
        + deletedTarget
        + '\''
        + ", deletedResolveInfo='"
        + deletedResolveInfo
        + '\''
        + '}';
  }
}
