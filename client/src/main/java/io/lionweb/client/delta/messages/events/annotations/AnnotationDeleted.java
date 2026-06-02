package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Existing node deletedAnnotation, and all its deletedDescendants, have been deleted from parent's
 * annotations at index.
 */
public class AnnotationDeleted extends BaseDeltaEvent<AnnotationDeleted> {
  public final @NotNull String deletedAnnotation;
  public final @NotNull String[] deletedDescendants;
  public final @NotNull String parent;
  public final int index;

  public AnnotationDeleted(
      int sequenceNumber,
      @NotNull String deletedAnnotation,
      @NotNull String[] deletedDescendants,
      @NotNull String parent,
      int index) {
    super(sequenceNumber);
    Objects.requireNonNull(deletedAnnotation, "deletedAnnotation should not be null");
    Objects.requireNonNull(deletedDescendants, "deletedDescendants should not be null");
    Objects.requireNonNull(parent, "parent should not be null");
    if (index < 0) {
      throw new IllegalArgumentException("index should be non-negative");
    }
    this.deletedAnnotation = deletedAnnotation;
    this.deletedDescendants = deletedDescendants;
    this.parent = parent;
    this.index = index;
  }

  @Override
  public String toString() {
    return "AnnotationDeleted{"
        + "deletedAnnotation='"
        + deletedAnnotation
        + '\''
        + ", deletedDescendants="
        + Arrays.toString(deletedDescendants)
        + ", parent='"
        + parent
        + '\''
        + ", index="
        + index
        + '}';
  }
}
