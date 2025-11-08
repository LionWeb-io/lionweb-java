package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;

/**
 * Existing node movedAnnotation (previously inside oldParent's annotations at oldIndex) has been
 * moved inside newParent's annotations at newIndex.
 */
public class AnnotationMovedFromOtherParent extends BaseDeltaEvent {
  public final String newParent;
  public final int newIndex;
  public final String movedAnnotation;
  public final String oldParent;
  public final int oldIndex;

  public AnnotationMovedFromOtherParent(
      int sequenceNumber,
      String newParent,
      int newIndex,
      String movedAnnotation,
      String oldParent,
      int oldIndex) {
    super(sequenceNumber);
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
