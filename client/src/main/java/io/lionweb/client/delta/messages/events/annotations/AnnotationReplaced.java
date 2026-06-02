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
public class AnnotationReplaced extends BaseDeltaEvent {
  public final String parent;
  public final SerializationChunk newAnnotation;
  public final String replacedAnnotation;
  public final String[] replacedDescendants;
  public final int index;

  public AnnotationReplaced(
      int sequenceNumber,
      @NotNull SerializationChunk newAnnotation,
      @NotNull String replacedAnnotation,
      @NotNull String[] replacedDescendants,
      @NotNull String parent,
      int index) {
    super(sequenceNumber);
    Objects.requireNonNull(newAnnotation, "newAnnotation cannot be null");
    Objects.requireNonNull(replacedAnnotation, "replacedAnnotation cannot be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants cannot be null");
    Objects.requireNonNull(parent, "parent cannot be null");
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
