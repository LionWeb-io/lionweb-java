package io.lionweb.client.delta.messages.events.children;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class ChildAdded extends BaseDeltaEvent<ChildAdded> {
  public final String parent;
  public final SerializationChunk newChild;
  public final MetaPointer containment;
  public final int index;

  public ChildAdded(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull SerializationChunk newChild,
      @NotNull MetaPointer containment,
      int index) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent cannot be null");
    Objects.requireNonNull(newChild, "newChild cannot be null");
    Objects.requireNonNull(containment, "containment cannot be null");
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
