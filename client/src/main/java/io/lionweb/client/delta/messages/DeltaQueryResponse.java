package io.lionweb.client.delta.messages;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class DeltaQueryResponse {
  public final @NotNull String queryId;
  public final List<ProtocolMessage> protocolMessages = new LinkedList<>();
  public final Map<String, Object> values = new HashMap<>();

  public DeltaQueryResponse(@NotNull String queryId) {
    this.queryId = queryId;
  }
}
