package io.lionweb.client.delta.messages.queries.partitcipations;

import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

public class Reconnect extends DeltaQuery {
  public final String participationId;
  public final String lastReceivedSequenceNumber;

  public Reconnect(
      @NotNull String queryId, String participationId, String lastReceivedSequenceNumber) {
    super(queryId);
    this.participationId = participationId;
    this.lastReceivedSequenceNumber = lastReceivedSequenceNumber;
  }
}
