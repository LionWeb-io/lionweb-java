package io.lionweb.client.delta.messages.events.references;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * newResolveInfo has been added as ResolveInfo to existing entry inside parent's reference at index
 * with target.
 */
public class ReferenceResolveInfoAdded extends BaseDeltaEvent<ReferenceResolveInfoAdded> {
  public final @NotNull String parent;
  public final @NotNull MetaPointer reference;
  public final int index;
  public final @NotNull String newResolveInfo;
  public final @Nullable String target;

  public ReferenceResolveInfoAdded(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @NotNull String newResolveInfo,
      @Nullable String target) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(reference, "reference should not be null");
    Objects.requireNonNull(newResolveInfo, "newResolveInfo should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index should be non-negative");
    }
    this.parent = parent;
    this.reference = reference;
    this.index = index;
    this.newResolveInfo = newResolveInfo;
    this.target = target;
  }

  @Override
  public String toString() {
    return "ReferenceResolveInfoAdded{"
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
        + '}';
  }
}
