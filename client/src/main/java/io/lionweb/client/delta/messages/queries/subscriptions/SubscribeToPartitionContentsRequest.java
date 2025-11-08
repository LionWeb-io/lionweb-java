package io.lionweb.client.delta.messages.queries.subscriptions;

import io.lionweb.client.delta.messages.DeltaQuery;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class SubscribeToPartitionContentsRequest extends DeltaQuery {

  /** TargetNode Node id of the partition this client wants to receive events of. */
  private @NotNull String partition;

  public SubscribeToPartitionContentsRequest(@NotNull String queryId, @NotNull String partition) {
    super(queryId);
    Objects.requireNonNull(partition, "partition must not be null");
    this.partition = partition;
  }

  @Override
  public String toString() {
    return "SubscribeToPartitionContentsRequest{"
        + "partition='"
        + partition
        + '\''
        + ", queryId='"
        + queryId
        + '\''
        + '}';
  }
}
