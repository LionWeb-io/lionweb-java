package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;

public class ClassifierChanged extends BaseDeltaEvent {
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

  @Override
  public String toString() {
    return "ClassifierChanged{"
        + "node='"
        + node
        + '\''
        + ", newClassifier="
        + newClassifier
        + ", oldClassifier="
        + oldClassifier
        + '}';
  }
}
