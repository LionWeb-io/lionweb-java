package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Represents an abstract base class for a Delta Query in the Delta framework. */
public abstract class DeltaQuery {
  public final @NotNull String queryId;
  public final List<ProtocolMessage> protocolMessages = new LinkedList<>();

  public DeltaQuery(@NotNull String queryId) {
    Objects.requireNonNull(queryId, "queryId must not be null");
    this.queryId = queryId;
  }
}
