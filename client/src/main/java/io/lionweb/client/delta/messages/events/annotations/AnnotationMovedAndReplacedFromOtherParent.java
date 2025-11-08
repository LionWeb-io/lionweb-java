package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.Arrays;

/**
 * Existing node movedAnnotation (previously inside oldParent's annotations at oldIndex) has
 * replaced the existing replacedAnnotation inside newParent's annotations at newIndex.
 */
public class AnnotationMovedAndReplacedFromOtherParent extends BaseDeltaEvent {
  public final String newParent;
  public final int newIndex;
  public final String movedAnnotation;
  public final String oldParent;
  public final int oldIndex;
  public final String replacedAnnotation;
  public final String[] replacedDescendants;

  public AnnotationMovedAndReplacedFromOtherParent(
      int sequenceNumber,
      String newParent,
      int newIndex,
      String movedAnnotation,
      String oldParent,
      int oldIndex,
      String replacedAnnotation,
      String[] replacedDescendants) {
    super(sequenceNumber);
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
