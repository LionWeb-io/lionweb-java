package io.lionweb.client.delta.messages.queries.subscriptions;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class SubscribeToPartitionContentsResponse extends DeltaQueryResponse {

  public final @NotNull SerializationChunk contents;

  public SubscribeToPartitionContentsResponse(
      @NotNull String queryId, @NotNull SerializationChunk contents) {
    super(queryId);
    Objects.requireNonNull(contents, "contents must not be null");
    this.contents = contents;
  }

  @Override
  public String toString() {
    return "SubscribeToPartitionContentsResponse{"
        + "contents="
        + contents
        + ", queryId='"
        + queryId
        + '\''
        + '}';
  }
}
