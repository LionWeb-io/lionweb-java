package io.lionweb.client.delta.messages.queries.subscriptions;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

public class SubscribeToChangingPartitionsRequest extends DeltaQuery {
  /**
   * Whether this client wants to receive events on newly created partitions (true), or not (false)
   */
  private boolean creation;

  /** Whether this client wants to receive events on deleted partitions (true), or not (false). */
  private boolean deletion;

  /**
   * Whether this client wants to automatically subscribe to newly created partitions (true), or not
   * (false).
   */
  private boolean partitions;

  public SubscribeToChangingPartitionsRequest(
      @NotNull String queryId, boolean creation, boolean deletion, boolean partitions) {
    super(queryId);
    this.creation = creation;
    this.deletion = deletion;
    this.partitions = partitions;
  }

  @Override
  public String toString() {
    return "SubscribeToChangingPartitionsRequest{"
        + "creation="
        + creation
        + ", deletion="
        + deletion
        + ", partitions="
        + partitions
        + '}';
  }
}
