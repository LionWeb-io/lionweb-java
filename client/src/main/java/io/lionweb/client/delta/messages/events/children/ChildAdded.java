package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildAdded extends BaseDeltaEvent<ChildAdded> {
  public final @NotNull String parent;
  public final @NotNull SerializationChunk newChild;
  public final @NotNull MetaPointer containment;
  public final int index;

  public ChildAdded(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull SerializationChunk newChild,
      @NotNull MetaPointer containment,
      int index) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(newChild, "newChild should not be null");
    Objects.requireNonNull(containment, "containment should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index should be non-negative");
    }
    this.parent = parent;
    this.newChild = newChild;
    this.containment = containment;
    this.index = index;
  }

  @Override
  public String toString() {
    return "ChildAdded{"
        + "parent='"
        + parent
        + '\''
        + ", newChild="
        + newChild
        + ", containment="
        + containment
        + ", index="
        + index
        + '}';
  }
}
