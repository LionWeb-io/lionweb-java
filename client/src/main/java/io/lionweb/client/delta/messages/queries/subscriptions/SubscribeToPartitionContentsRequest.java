package io.lionweb.client.delta.messages.queries.subscriptions;

import io.lionweb.client.delta.messages.DeltaQuery;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a request to subscribe to the contents of a specific partition. This class allows a
 * client to request events pertaining to a given partition identified by its node ID.
 */
public class SubscribeToPartitionContentsRequest extends DeltaQuery {

  /** Node id of the partition this client wants to receive events of. */
  public final @NotNull String partition;

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
