package io.lionweb.client.delta.messages.events.partitions;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.SerializationChunk;

public class PartitionAdded extends BaseDeltaEvent {

  public final SerializationChunk newPartition;

  public PartitionAdded(int sequenceNumber, SerializationChunk newPartition) {
    super(sequenceNumber);
    this.newPartition = newPartition;
  }

  @Override
  public String toString() {
    return "PartitionAdded{" + "newPartition=" + newPartition + '}';
  }
}
