package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Existing node movedAnnotation (previously inside oldParent's annotations at oldIndex) has
 * replaced the existing replacedAnnotation inside newParent's annotations at newIndex.
 */
public class AnnotationMovedAndReplacedFromOtherParent extends BaseDeltaEvent {
  public final @NotNull String newParent;
  public final int newIndex;
  public final @NotNull String movedAnnotation;
  public final @NotNull String oldParent;
  public final int oldIndex;
  public final @NotNull String replacedAnnotation;
  public final @NotNull String[] replacedDescendants;

  public AnnotationMovedAndReplacedFromOtherParent(
      int sequenceNumber,
      @NotNull String newParent,
      int newIndex,
      @NotNull String movedAnnotation,
      @NotNull String oldParent,
      int oldIndex,
      @NotNull String replacedAnnotation,
      @NotNull String[] replacedDescendants) {
    super(sequenceNumber);
    Objects.requireNonNull(newParent, "newParent cannot be null");
    Objects.requireNonNull(movedAnnotation, "movedAnnotation cannot be null");
    Objects.requireNonNull(oldParent, "oldParent cannot be null");
    Objects.requireNonNull(replacedAnnotation, "replacedAnnotation cannot be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants cannot be null");
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be positive");
    }
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex should be positive");
    }
    this.newParent = newParent;
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
    this.oldParent = oldParent;
    this.oldIndex = oldIndex;
    this.replacedAnnotation = replacedAnnotation;
    this.replacedDescendants = replacedDescendants;
  }

  @Override
  public String toString() {
    return "AnnotationMovedAndReplacedFromOtherParent{"
        + "newParent='"
        + newParent
        + '\''
        + ", newIndex="
        + newIndex
        + ", movedAnnotation='"
        + movedAnnotation
        + '\''
        + ", oldParent='"
        + oldParent
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
