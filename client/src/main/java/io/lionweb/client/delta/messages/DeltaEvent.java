package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;

public abstract class DeltaEvent {
  public final List<ProtocolMessage> protocolMessages = new LinkedList<>();
}
