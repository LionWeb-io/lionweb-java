package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** New node newAnnotation has been added to parent's annotations at index. */
public class AnnotationAdded extends BaseDeltaEvent<AnnotationAdded> {
  public final String parent;
  public final SerializationChunk newAnnotation;
  public final int index;

  public AnnotationAdded(
      int sequenceNumber,
      @NotNull String parent,
      @NotNull SerializationChunk newAnnotation,
      int index) {
    super(sequenceNumber);
    Objects.requireNonNull(parent, "parent cannot be null");
    Objects.requireNonNull(newAnnotation, "newAnnotation cannot be null");
    this.parent = parent;
    this.newAnnotation = newAnnotation;
    this.index = index;
  }

  @Override
  public String toString() {
    return "AnnotationAdded{"
        + "parent='"
        + parent
        + '\''
        + ", newAnnotation="
        + newAnnotation
        + ", index="
        + index
        + '}';
  }
}
