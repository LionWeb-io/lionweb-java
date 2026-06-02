package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.MetaPointer;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ClassifierChanged extends BaseDeltaEvent {
  public String node;
  public MetaPointer newClassifier;
  public MetaPointer oldClassifier;

  public ClassifierChanged(
      int sequenceNumber,
      @NotNull String node,
      @NotNull MetaPointer newClassifier,
      @NotNull MetaPointer oldClassifier) {
    super(sequenceNumber);
    Objects.requireNonNull(node, "node cannot be null");
    Objects.requireNonNull(newClassifier, "newClassifier cannot be null");
    Objects.requireNonNull(oldClassifier, "oldClassifier cannot be null");
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
