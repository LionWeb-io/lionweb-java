package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class DeltaQuery {
  public final @NotNull String queryId;
  public final List<ProtocolMessage> protocolMessages = new LinkedList<>();

  public DeltaQuery(@NotNull String queryId) {
    this.queryId = queryId;
  }
}
