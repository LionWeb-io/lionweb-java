package io.lionweb.client.delta.messages.queries.partitcipations;

import io.lionweb.client.delta.messages.DeltaQuery;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class ReconnectRequest extends DeltaQuery {
  public final @NotNull String participationId;
  public final long lastReceivedSequenceNumber;

  public ReconnectRequest(
      @NotNull String queryId, @NotNull String participationId, long lastReceivedSequenceNumber) {
    super(queryId);
    Objects.requireNonNull(participationId, "participationId must not be null");
    this.participationId = participationId;
    this.lastReceivedSequenceNumber = lastReceivedSequenceNumber;
  }

  @Override
  public String toString() {
    return "ReconnectRequest{"
        + "participationId='"
        + participationId
        + '\''
        + ", lastReceivedSequenceNumber="
        + lastReceivedSequenceNumber
        + '}';
  }
}
