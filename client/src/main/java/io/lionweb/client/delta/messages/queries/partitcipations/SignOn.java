package io.lionweb.client.delta.messages.queries.partitcipations;

import io.lionweb.client.delta.DeltaProtocolVersion;
import io.lionweb.client.delta.messages.DeltaQuery;
import org.jetbrains.annotations.NotNull;

public class SignOn extends DeltaQuery {
  public final @NotNull DeltaProtocolVersion deltaProtocolVersion;
  public final @NotNull String clientId;

  public SignOn(
      @NotNull String queryId,
      @NotNull DeltaProtocolVersion deltaProtocolVersion,
      @NotNull String clientId) {
    super(queryId);
    this.deltaProtocolVersion = deltaProtocolVersion;
    this.clientId = clientId;
  }
}
