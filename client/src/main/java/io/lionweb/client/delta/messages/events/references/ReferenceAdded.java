package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.Nullable;

/** Reference with newTarget/newResolveInfo has been added to parent's reference at index. */
public class ReferenceAdded extends CommonDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final @Nullable String newTarget;
  public final @Nullable String newResolveInfo;

  public ReferenceAdded(
      int sequenceNumber,
      String parent,
      MetaPointer reference,
      int index,
      @Nullable String newTarget,
      @Nullable String newResolveInfo) {
    super(sequenceNumber);
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newTarget = newTarget;
    this.newResolveInfo = newResolveInfo;
  }
}
