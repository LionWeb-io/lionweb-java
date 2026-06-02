package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Existing reference with deletedTarget/deletedResolveInfo has been deleted from parent's reference
 * at index.
 */
public class ReferenceDeleted extends BaseDeltaEvent<ReferenceDeleted> {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;

  /** Deleted target node id. */
  public final @Nullable String deletedReference;

  public final @Nullable String deletedResolveInfo;

  public ReferenceDeleted(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String deletedReference,
      @Nullable String deletedResolveInfo) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(reference, "reference should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index should be non-negative");
    }
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.deletedReference = deletedReference;
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
        + ", deletedReference='"
        + deletedReference
        + '\''
        + ", deletedResolveInfo='"
        + deletedResolveInfo
        + '\''
        + '}';
  }
}
