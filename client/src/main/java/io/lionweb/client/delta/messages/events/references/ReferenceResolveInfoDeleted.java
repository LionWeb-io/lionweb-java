package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ResolveInfo deletedResolveInfo has been deleted from existing entry inside parent's reference at
 * index with target.
 */
public class ReferenceResolveInfoDeleted extends BaseDeltaEvent<ReferenceResolveInfoDeleted> {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String target;
  public final String deletedResolveInfo;

  public ReferenceResolveInfoDeleted(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @Nullable String target,
      @NotNull String deletedResolveInfo) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent cannot be null");
    Objects.requireNonNull(reference, "reference cannot be null");
    Objects.requireNonNull(deletedResolveInfo, "deletedResolveInfo cannot be null");
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.target = target;
    this.deletedResolveInfo = deletedResolveInfo;
  }

  @Override
  public String toString() {
    return "ReferenceResolveInfoDeleted{"
        + "parent='"
        + parent
        + '\''
        + ", reference="
        + reference
        + ", index="
        + index
        + ", target='"
        + target
        + '\''
        + ", deletedResolveInfo='"
        + deletedResolveInfo
        + '\''
        + '}';
  }
}
