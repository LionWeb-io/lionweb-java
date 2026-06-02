package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Existing node movedAnnotation (previously inside oldParent's annotations at oldIndex) has been
 * moved inside newParent's annotations at newIndex.
 */
public class AnnotationMovedFromOtherParent extends BaseDeltaEvent<AnnotationMovedFromOtherParent> {
  public final @NotNull String newParent;
  public final int newIndex;
  public final @NotNull String movedAnnotation;
  public final @NotNull String oldParent;
  public final int oldIndex;

  public AnnotationMovedFromOtherParent(
      int sequenceNumber,
      @NotNull String newParent,
      int newIndex,
      @NotNull String movedAnnotation,
      @NotNull String oldParent,
      int oldIndex) {
    super(sequenceNumber);
    Objects.requireNonNull(newParent, "newParent should not be null");
    Objects.requireNonNull(movedAnnotation, "movedAnnotation should not be null");
    Objects.requireNonNull(oldParent, "oldParent should not be null");
    if (newIndex < 0) {
      throw new IllegalArgumentException("newIndex should be non-negative");
    }
    if (oldIndex < 0) {
      throw new IllegalArgumentException("oldIndex should be non-negative");
    }
    this.newParent = newParent;
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
    this.oldParent = oldParent;
    this.oldIndex = oldIndex;
  }

  @Override
  public String toString() {
    return "AnnotationMovedFromOtherParent{"
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
        + '}';
  }
}
