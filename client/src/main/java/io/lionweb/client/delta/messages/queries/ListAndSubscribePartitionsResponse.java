package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Response carrying partitions for a ListAndSubscribePartitionsRequest. */
public class ListAndSubscribePartitionsResponse extends DeltaQueryResponse {
  /** Whether this message is a continuation of a split/chunked sequence. Absent when false. */
  public final boolean split;

  /** The partitions returned by the server. */
  public final @NotNull SerializationChunk partitions;

  public ListAndSubscribePartitionsResponse(
      @NotNull String queryId, @NotNull SerializationChunk partitions, boolean split) {
    super(queryId);
    Objects.requireNonNull(partitions, "partitions must not be null");
    this.partitions = partitions;
    this.split = split;
  }

  @Override
  public String toString() {
    return "ListAndSubscribePartitionsResponse{"
        + "split="
        + split
        + ", partitions="
        + partitions
        + ", queryId='"
        + queryId
        + '\''
        + '}';
  }
}
