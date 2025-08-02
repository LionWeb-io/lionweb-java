package io.lionweb.client.delta.messages;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public abstract class DeltaQuery {
    public final @NotNull String queryId;
    public final List<ProtocolMessage> protocolMessages = new LinkedList<>();

    public DeltaQuery(@NotNull String queryId) {
        this.queryId = queryId;
    }
}
