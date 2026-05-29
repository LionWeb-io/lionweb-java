package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the response for a query that lists partitions in the Delta framework.
 *
 * <p>This response includes information about the partitions returned by the server and indicates
 * whether the response is part of a split or chunked sequence.
 *
 * <p>This class extends {@code DeltaQueryResponse}, inheriting its unique query identifier
 * functionality.
 */
public class ListPartitionsResponse extends DeltaQueryResponse {
  /** Whether this message is a continuation of a split/chunked sequence. Absent when false. */
  public Boolean split;

  /** The partitions returned by the server. */
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
