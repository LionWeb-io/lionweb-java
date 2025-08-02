package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;

public class DeltaCommand {
    public String commandId;
    public List<ProtocolMessage> protocolMessages = new LinkedList<>();

    public DeltaCommand(String commandId) {
        this.commandId = commandId;
    }
}
