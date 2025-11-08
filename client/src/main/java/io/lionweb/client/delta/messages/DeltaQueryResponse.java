package io.lionweb.client.delta.messages;

import java.util.*;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an abstract base class for handling responses to Delta Query objects in the Delta
 * framework. A DeltaQueryResponse is uniquely identified by a `queryId` and includes properties to
 * store protocol messages and arbitrary values.
 */
public abstract class DeltaQueryResponse {
  public final @NotNull String queryId;
  public final List<ProtocolMessage> protocolMessages = new LinkedList<>();
  public final Map<String, Object> values = new HashMap<>();

  public DeltaQueryResponse(@NotNull String queryId) {
    Objects.requireNonNull(queryId, "queryId must not be null");
    this.queryId = queryId;
  }
}
