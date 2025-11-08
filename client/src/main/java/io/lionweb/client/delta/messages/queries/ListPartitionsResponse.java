package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ListPartitionsResponse extends DeltaQueryResponse {
  public @NotNull SerializationChunk partitions;

  public ListPartitionsResponse(@NotNull String queryId, @NotNull SerializationChunk partitions) {
    super(queryId);
    Objects.requireNonNull(partitions, "partitions must not be null");
    this.partitions = partitions;
  }

  @Override
  public String toString() {
    return "ListPartitionsResponse{"
        + "partitions="
        + partitions
        + ", queryId='"
        + queryId
        + '\''
        + '}';
  }
}
