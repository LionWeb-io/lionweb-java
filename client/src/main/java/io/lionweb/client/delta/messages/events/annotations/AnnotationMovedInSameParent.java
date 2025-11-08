package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.BaseDeltaEvent;

/**
 * Existing node movedAnnotation (previously inside parent's annotations at oldIndex) has been moved
 * inside parent's annotations at newIndex.
 */
public class AnnotationMovedInSameParent extends BaseDeltaEvent {
  public final int newIndex;
  public final String movedAnnotation;
  public final String parent;
  public final int oldIndex;

  public AnnotationMovedInSameParent(
      int sequenceNumber, int newIndex, String movedAnnotation, String parent, int oldIndex) {
    super(sequenceNumber);
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
