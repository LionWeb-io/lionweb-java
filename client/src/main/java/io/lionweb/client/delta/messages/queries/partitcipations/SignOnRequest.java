package io.lionweb.client.delta.messages.queries.partitcipations;

import io.lionweb.client.delta.DeltaProtocolVersion;
import io.lionweb.client.delta.messages.DeltaQuery;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class SignOnRequest extends DeltaQuery {
  public final @NotNull DeltaProtocolVersion deltaProtocolVersion;
  public final @NotNull String clientId;

  public SignOnRequest(
      @NotNull String queryId,
      @NotNull DeltaProtocolVersion deltaProtocolVersion,
      @NotNull String clientId) {
    super(queryId);
    Objects.requireNonNull(deltaProtocolVersion, "deltaProtocolVersion must not be null");
    Objects.requireNonNull(clientId, "clientId must not be null");
    this.deltaProtocolVersion = deltaProtocolVersion;
    this.clientId = clientId;
  }

  @Override
  public String toString() {
    return "SignOnRequest{"
        + "deltaProtocolVersion="
        + deltaProtocolVersion
        + ", clientId='"
        + clientId
        + '\''
        + ", queryId='"
        + queryId
        + '\''
        + '}';
  }
}
