package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.SerializationChunk;

/**
 * Existing node replacedAnnotation, and all its replacedDescendants, inside parent's annotations at
 * index has been replaced with new node newAnnotation.
 */
public class AnnotationReplaced extends CommonDeltaEvent {
  public final SerializationChunk newAnnotation;
  public final String replacedAnnotation;
  public final String[] replacedDescendants;
  public final String parent;
  public final int index;

  public AnnotationReplaced(
      int sequenceNumber,
      SerializationChunk newAnnotation,
      String replacedAnnotation,
      String[] replacedDescendants,
      String parent,
      int index) {
    super(sequenceNumber);
    this.newAnnotation = newAnnotation;
    this.replacedAnnotation = replacedAnnotation;
    this.replacedDescendants = replacedDescendants;
    this.parent = parent;
    this.index = index;
  }
}
