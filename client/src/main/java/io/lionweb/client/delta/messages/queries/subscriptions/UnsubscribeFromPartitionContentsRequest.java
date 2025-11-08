package io.lionweb.client.delta.messages.queries.subscriptions;

import io.lionweb.client.delta.messages.DeltaQuery;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class UnsubscribeFromPartitionContentsRequest extends DeltaQuery {

  public final @NotNull String partition;

  public UnsubscribeFromPartitionContentsRequest(
      @NotNull String queryId, @NotNull String partition) {
    super(queryId);
    Objects.requireNonNull(partition, "partition must not be null");
    this.partition = partition;
  }

  @Override
  public String toString() {
    return "UnsubscribeFromPartitionContentsRequest{"
        + "partition='"
        + partition
        + '\''
        + ", queryId='"
        + queryId
        + '\''
        + '}';
  }
}
