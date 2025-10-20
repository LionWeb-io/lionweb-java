package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.CommonDeltaEvent;

/**
 * Existing node deletedAnnotation, and all its deletedDescendants, have been deleted from parent's
 * annotations at index.
 */
public class AnnotationDeleted extends CommonDeltaEvent {
  public final String deletedAnnotation;
  public final String[] deletedDescendants;
  public final String parent;
  public final int index;

  public AnnotationDeleted(
      int sequenceNumber,
      String deletedAnnotation,
      String[] deletedDescendants,
      String parent,
      int index) {
    super(sequenceNumber);
    this.deletedAnnotation = deletedAnnotation;
    this.deletedDescendants = deletedDescendants;
    this.parent = parent;
    this.index = index;
  }
}
