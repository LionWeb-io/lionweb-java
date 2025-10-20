package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.CommonDeltaEvent;

/**
 * Existing node movedAnnotation (previously inside parent's annotations at oldIndex) has replaced
 * the existing replacedAnnotation inside parent's annotations at newIndex.
 */
public class AnnotationMovedAndReplacedInSameParent extends CommonDeltaEvent {
  public final int newIndex;
  public final String movedAnnotation;
  public final String parent;
  public final int oldIndex;
  public final String replacedAnnotation;
  public final String[] replacedDescendants;

  public AnnotationMovedAndReplacedInSameParent(
      int sequenceNumber,
      int newIndex,
      String movedAnnotation,
      String parent,
      int oldIndex,
      String replacedAnnotation,
      String[] replacedDescendants) {
    super(sequenceNumber);
    this.newIndex = newIndex;
    this.movedAnnotation = movedAnnotation;
    this.parent = parent;
    this.oldIndex = oldIndex;
    this.replacedAnnotation = replacedAnnotation;
    this.replacedDescendants = replacedDescendants;
  }
}
