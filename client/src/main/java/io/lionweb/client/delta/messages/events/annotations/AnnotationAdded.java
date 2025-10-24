package io.lionweb.client.delta.messages.events.annotations;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.SerializationChunk;

/** New node newAnnotation has been added to parent's annotations at index. */
public class AnnotationAdded extends CommonDeltaEvent {
  public final String parent;
  public final SerializationChunk newAnnotation;
  public final int index;

  public AnnotationAdded(
      int sequenceNumber, String parent, SerializationChunk newAnnotation, int index) {
    super(sequenceNumber);
    this.parent = parent;
    this.newAnnotation = newAnnotation;
    this.index = index;
  }
}
