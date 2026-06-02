package io.lionweb.client.delta.messages.queries.subscriptions;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

/**
 * Query asking to receive {@link io.lionweb.client.delta.messages.events.partitions.PartitionAdded}
 * and/or {@link io.lionweb.client.delta.messages.events.partitions.PartitionDeleted} events.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API
 *     specification</a>
 */
public class SubscribeToChangingPartitionsRequest extends DeltaQuery {
  /**
   * Whether this client wants to receive events on newly created partitions (true), or not (false)
   */
  private boolean creation;

  /** Whether this client wants to receive events on deleted partitions (true), or not (false). */
  private boolean deletion;

  public SubscribeToChangingPartitionsRequest(
      @NotNull String queryId, boolean creation, boolean deletion) {
    super(queryId);
    this.creation = creation;
    this.deletion = deletion;
  }

  @Override
  public String toString() {
    return "SubscribeToChangingPartitionsRequest{"
        + "creation="
        + creation
        + ", deletion="
        + deletion
        + '}';
  }
}
