package io.lionweb.client.delta.messages.events.partitions;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.List;

/**
 * Delta event fired when a partition and all its descendants are removed from the repository.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API specification</a>
 */
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
