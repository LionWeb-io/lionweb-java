package io.lionweb.client.delta.messages.queries.partitcipations;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class SignOnResponse extends DeltaQueryResponse {

  public final @NotNull String participationId;

  public SignOnResponse(@NotNull String queryId, @NotNull String participationId) {
    super(queryId);
    Objects.requireNonNull(participationId, "participationId must not be null");
    this.participationId = participationId;
  }

  @Override
  public String toString() {
    return "SignOnResponse{"
        + "participationId='"
        + participationId
        + '\''
        + ", queryId='"
        + queryId
        + '\''
        + '}';
  }
}
