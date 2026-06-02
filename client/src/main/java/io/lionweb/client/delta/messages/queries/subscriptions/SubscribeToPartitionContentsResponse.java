package io.lionweb.client.delta.messages.queries.subscriptions;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Response to a subscribe-to-partition-contents request, carrying the initial partition snapshot.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API specification</a>
 */
public class SubscribeToPartitionContentsResponse extends DeltaQueryResponse {
  /** Whether this message is a continuation of a split/chunked sequence. Absent when false. */
  public final boolean split;

  /** The contents of the subscribed partition. */
  public final @NotNull SerializationChunk contents;

  public SubscribeToPartitionContentsResponse(
      @NotNull String queryId, @NotNull SerializationChunk contents, boolean split) {
    super(queryId);
    Objects.requireNonNull(contents, "contents must not be null");
    this.contents = contents;
    this.split = split;
  }

  public SubscribeToPartitionContentsResponse(
      @NotNull String queryId, @NotNull SerializationChunk contents) {
    this(queryId, contents, false);
  }
}
