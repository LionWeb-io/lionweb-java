package io.lionweb.client.delta.messages.events.partitions;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Delta event fired when a partition and all its descendants are removed from the repository.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API
 *     specification</a>
 */
public class PartitionDeleted extends BaseDeltaEvent<PartitionDeleted> {

  public final @NotNull String deletedPartition;
  public final @NotNull List<String> deletedDescendants;

  public PartitionDeleted(
      int sequenceNumber,
      @NotNull String deletedPartition,
      @NotNull List<String> deletedDescendants) {
    super(sequenceNumber);
    Objects.requireNonNull(deletedPartition, "deletedPartition should not be null");
    Objects.requireNonNull(deletedDescendants, "deletedDescendants should not be null");
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
