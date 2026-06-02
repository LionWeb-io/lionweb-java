package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Existing node replacedAnnotation, and all its replacedDescendants, inside parent's annotations at
 * index has been replaced with new node newAnnotation.
 */
public class AnnotationReplaced extends BaseDeltaEvent<AnnotationReplaced> {
  public final @NotNull String parent;
  public final @NotNull SerializationChunk newAnnotation;
  public final @NotNull String replacedAnnotation;
  public final @NotNull String[] replacedDescendants;
  public final int index;

  public AnnotationReplaced(
      int sequenceNumber,
      @NotNull SerializationChunk newAnnotation,
      @NotNull String replacedAnnotation,
      @NotNull String[] replacedDescendants,
      @NotNull String parent,
      int index) {
    super(sequenceNumber);
    Objects.requireNonNull(newAnnotation, "newAnnotation should not be null");
    Objects.requireNonNull(replacedAnnotation, "replacedAnnotation should not be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants should not be null");
    Objects.requireNonNull(parent, "parent should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index should be non-negative");
    }
    this.newAnnotation = newAnnotation;
    this.replacedAnnotation = replacedAnnotation;
    this.replacedDescendants = replacedDescendants;
    this.parent = parent;
    this.index = index;
  }

  @Override
  public String toString() {
    return "AnnotationReplaced{"
        + "newAnnotation="
        + newAnnotation
        + ", replacedAnnotation='"
        + replacedAnnotation
        + '\''
        + ", replacedDescendants="
        + Arrays.toString(replacedDescendants)
        + ", parent='"
        + parent
        + '\''
        + ", index="
        + index
        + '}';
  }
}
