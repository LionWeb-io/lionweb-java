package io.lionweb.client.delta.messages.events.partitions;

import io.lionweb.client.delta.messages.BaseDeltaEvent;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class PartitionDeleted extends BaseDeltaEvent {

  public final String deletedPartition;
  public final List<String> deletedDescendants;

  public PartitionDeleted(
      int sequenceNumber,
      @NotNull String deletedPartition,
      @NotNull List<String> deletedDescendants) {
    super(sequenceNumber);
    Objects.requireNonNull(deletedPartition, "deletedPartition cannot be null");
    Objects.requireNonNull(deletedDescendants, "deletedDescendants cannot be null");
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
