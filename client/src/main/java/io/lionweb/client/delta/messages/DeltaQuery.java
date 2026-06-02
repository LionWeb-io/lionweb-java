package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base for query messages sent by a client to the server in the LionWeb Delta protocol.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API specification</a>
 */
public abstract class DeltaQuery {
  /** Represents the unique identifier for this Delta Query. */
  public final @NotNull String queryId;

  /**
   * Represents additional information associated with a protocol message in the Delta framework.
   */
  public final List<AdditionalInfo> additionalInfos = new LinkedList<>();

  public DeltaQuery(@NotNull String queryId) {
    Objects.requireNonNull(queryId, "queryId must not be null");
    this.queryId = queryId;
  }
}
