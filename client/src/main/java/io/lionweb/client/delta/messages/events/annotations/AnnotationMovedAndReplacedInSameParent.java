package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Existing node movedAnnotation (previously inside parent's annotations at oldIndex) has replaced
 * the existing replacedAnnotation inside parent's annotations at newIndex.
 */
public class AnnotationMovedAndReplacedInSameParent
    extends BaseDeltaEvent<AnnotationMovedAndReplacedInSameParent> {
  public final int newIndex;
  public final @NotNull String movedAnnotation;
  public final @NotNull String parent;
  public final int oldIndex;
  public final @NotNull String replacedAnnotation;
  public final @NotNull String[] replacedDescendants;

  public AnnotationMovedAndReplacedInSameParent(
      int sequenceNumber,
      int newIndex,
      @NotNull String movedAnnotation,
      @NotNull String parent,
      int oldIndex,
      @NotNull String replacedAnnotation,
      @NotNull String[] replacedDescendants) {
    super(sequenceNumber);
    Objects.requireNonNull(movedAnnotation, "movedAnnotation should not be null");
    Objects.requireNonNull(parent, "parent should not be null");
    Objects.requireNonNull(replacedAnnotation, "replacedAnnotation should not be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants should not be null");
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex should be non-negative");
    }
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
    this.parent = parent;
    this.oldIndex = oldIndex;
    this.replacedAnnotation = replacedAnnotation;
    this.replacedDescendants = replacedDescendants;
  }

  @Override
  public String toString() {
    return "AnnotationMovedAndReplacedInSameParent{"
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
        + ", replacedAnnotation='"
        + replacedAnnotation
        + '\''
        + ", replacedDescendants="
        + Arrays.toString(replacedDescendants)
        + '}';
  }
}
