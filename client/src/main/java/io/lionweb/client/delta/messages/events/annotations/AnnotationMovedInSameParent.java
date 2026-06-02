package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Existing node movedAnnotation (previously inside parent's annotations at oldIndex) has been moved
 * inside parent's annotations at newIndex.
 */
public class AnnotationMovedInSameParent extends BaseDeltaEvent<AnnotationMovedInSameParent> {
  public final int newIndex;
  public final @NotNull String movedAnnotation;
  public final @NotNull String parent;
  public final int oldIndex;

  public AnnotationMovedInSameParent(
      int sequenceNumber,
      int newIndex,
      @NotNull String movedAnnotation,
      @NotNull String parent,
      int oldIndex) {
    super(sequenceNumber);
    Objects.requireNonNull(movedAnnotation, "movedAnnotation should not be null");
    Objects.requireNonNull(parent, "parent should not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex should be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
    this.parent = parent;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "AnnotationMovedInSameParent{"
        + "newIndex="
        + newIndex
        + ", movedAnnotation='"
        + movedAnnotation
        + '\''
        + ", parent='"
        + parent
        + '\''
        + ", oldIndex="
        + oldIndex
        + '}';
  }
}
