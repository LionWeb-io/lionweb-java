package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Existing node movedAnnotation (previously inside oldParent's annotations at oldIndex) has
 * replaced the existing replacedAnnotation inside newParent's annotations at indexOffset.
 */
public class AnnotationMovedAndReplacedFromOtherParent
    extends BaseDeltaEvent<AnnotationMovedAndReplacedFromOtherParent> {
  public final @NotNull String newParent;
  public final int indexOffset;
  public final @NotNull String movedAnnotation;
  public final @NotNull String oldParent;
  public final int oldIndex;
  public final @NotNull String replacedAnnotation;
  public final @NotNull String[] replacedDescendants;

  public AnnotationMovedAndReplacedFromOtherParent(
      int sequenceNumber,
      @NotNull String newParent,
      int indexOffset,
      @NotNull String movedAnnotation,
      @NotNull String oldParent,
      int oldIndex,
      @NotNull String replacedAnnotation,
      @NotNull String[] replacedDescendants) {
    super(sequenceNumber);
    Objects.requireNonNull(newParent, "newParent should not be null");
    Objects.requireNonNull(movedAnnotation, "movedAnnotation should not be null");
    Objects.requireNonNull(oldParent, "oldParent should not be null");
    Objects.requireNonNull(replacedAnnotation, "replacedAnnotation should not be null");
    Objects.requireNonNull(replacedDescendants, "replacedDescendants should not be null");
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
    if (indexOffset < 0) {
      throw new IllegalArgumentException("indexOffset should be non-negative");
    }
    this.newParent = newParent;
    this.indexOffset = indexOffset;
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
        + ", indexOffset="
        + indexOffset
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
