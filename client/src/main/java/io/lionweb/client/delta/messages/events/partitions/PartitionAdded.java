package io.lionweb.client.delta.messages.events.partitions;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import io.lionweb.serialization.data.SerializationChunk;

public class PartitionAdded extends CommonDeltaEvent {

  public final SerializationChunk newPartition;

  public PartitionAdded(int sequenceNumber, SerializationChunk newPartition) {
    super(sequenceNumber);
    this.newPartition = newPartition;
  }
}
