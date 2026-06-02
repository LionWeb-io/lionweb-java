package io.lionweb.client.delta.messages.events.partitions;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Delta event fired when a new partition is added to the repository.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API
 *     specification</a>
 */
public class PartitionAdded extends BaseDeltaEvent {

  public final SerializationChunk newPartition;

  public PartitionAdded(int sequenceNumber, @NotNull SerializationChunk newPartition) {
    super(sequenceNumber);
    Objects.requireNonNull(newPartition, "newPartition cannot be null");
    this.newPartition = newPartition;
  }

  @Override
  public String toString() {
    return "PartitionAdded{" + "newPartition=" + newPartition + '}';
  }
}
