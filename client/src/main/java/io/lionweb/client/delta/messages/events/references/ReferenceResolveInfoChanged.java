package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ResolveInfo of existing entry inside parent's reference at index with target has been changed
 * from oldResolveInfo to newResolveInfo.
 */
public class ReferenceResolveInfoChanged extends BaseDeltaEvent<ReferenceResolveInfoChanged> {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;
  public final @NotNull String newResolveInfo;
  public final @Nullable String target;
  public final @NotNull String oldResolveInfo;

  public ReferenceResolveInfoChanged(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @NotNull String newResolveInfo,
      @Nullable String target,
      @NotNull String oldResolveInfo) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(reference, "reference should not be null");
    Objects.requireNonNull(newResolveInfo, "newResolveInfo should not be null");
    Objects.requireNonNull(oldResolveInfo, "oldResolveInfo should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index should be non-negative");
    }
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
