package io.lionweb.client.delta.messages.queries.partitcipations;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import org.jetbrains.annotations.NotNull;

public class ReconnectResponse extends DeltaQueryResponse {
  public final long lastSentSequenceNumber;

  public ReconnectResponse(@NotNull String queryId, long lastSentSequenceNumber) {
    super(queryId);
    this.lastSentSequenceNumber = lastSentSequenceNumber;
  }

  @Override
  public String toString() {
    return "ReconnectResponse{" + "lastSentSequenceNumber=" + lastSentSequenceNumber + '}';
  }
}
