package io.lionweb.client.delta.messages.events.partitions;

import io.lionweb.client.delta.messages.CommonDeltaEvent;
import java.util.List;

public class PartitionDeleted extends CommonDeltaEvent {

  public final String deletedPartition;
  public final List<String> deletedDescendants;

  public PartitionDeleted(
      int sequenceNumber, String deletedPartition, List<String> deletedDescendants) {
    super(sequenceNumber);
    this.deletedPartition = deletedPartition;
    this.deletedDescendants = deletedDescendants;
  }
}
