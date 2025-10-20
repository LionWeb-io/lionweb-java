package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference with deletedTarget/deletedResolveInfo has been deleted from parent's reference
 * at index.
 */
public class ReferenceDeleted extends CommonDeltaEvent {
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
}
