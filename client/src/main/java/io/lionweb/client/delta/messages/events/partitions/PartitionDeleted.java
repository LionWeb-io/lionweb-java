package io.lionweb.client.delta.messages.events.partitions;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.List;

public class PartitionDeleted extends BaseDeltaEvent {

  public final String deletedPartition;
  public final List<String> deletedDescendants;

  public PartitionDeleted(
      int sequenceNumber, String deletedPartition, List<String> deletedDescendants) {
    super(sequenceNumber);
    this.deletedPartition = deletedPartition;
    this.deletedDescendants = deletedDescendants;
  }

  @Override
  public String toString() {
    return "PartitionDeleted{"
        + "deletedPartition='"
        + deletedPartition
        + '\''
        + ", deletedDescendants="
        + deletedDescendants
        + '}';
  }
}
