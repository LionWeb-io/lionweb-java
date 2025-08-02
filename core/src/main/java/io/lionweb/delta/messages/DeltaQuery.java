package io.lionweb.delta.messages;

import java.util.List;

public abstract class DeltaQuery {
    private String queryId;
    private List<ProtocolMessage> protocolMessages;
}
