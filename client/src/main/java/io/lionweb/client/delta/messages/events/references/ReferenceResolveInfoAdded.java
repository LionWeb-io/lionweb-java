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
public class ReferenceResolveInfoAdded extends BaseDeltaEvent {
  public final String parent;
  public final MetaPointer reference;
  public final int index;
  public final String newResolveInfo;
  public final String target;

  public ReferenceResolveInfoAdded(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull MetaPointer reference,
      int index,
      @NotNull String newResolveInfo,
      @Nullable String target) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent cannot be null");
    Objects.requireNonNull(reference, "reference cannot be null");
    Objects.requireNonNull(newResolveInfo, "newResolveInfo cannot be null");
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
