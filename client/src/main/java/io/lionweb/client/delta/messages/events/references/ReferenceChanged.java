package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference with oldTarget/oldResolveInfo inside parent's reference at index has been
 * replaced with newTarget/newResolveInfo.
 */
public class ReferenceChanged extends BaseDeltaEvent<ReferenceChanged> {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;

  /** New target node id. */
  public final @Nullable String newReference;

  public final @Nullable String newResolveInfo;

  /** Old target node id. */
  public final @Nullable String oldReference;

  public final @Nullable String oldResolveInfo;

  public ReferenceChanged(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String newReference,
      @Nullable String newResolveInfo,
      @Nullable String oldReference,
      @Nullable String oldResolveInfo) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(reference, "reference should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index should be non-negative");
    }
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
