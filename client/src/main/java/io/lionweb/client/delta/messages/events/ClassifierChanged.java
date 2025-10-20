package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public class ClassifierChanged extends CommonDeltaEvent {
  public String node;
  public MetaPointer newClassifier;
  public MetaPointer oldClassifier;

  public ClassifierChanged(
      int sequenceNumber, String node, MetaPointer newClassifier, MetaPointer oldClassifier) {
    super(sequenceNumber);
    this.node = node;
    this.newClassifier = newClassifier;
    this.oldClassifier = oldClassifier;
  }
}
