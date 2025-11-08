package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents an abstract base class for events in the delta framework. DeltaEvent serves as a
 * foundation for specific types of delta events that may involve communication via protocol
 * messages.
 */
public abstract class DeltaEvent {
  public final List<ProtocolMessage> protocolMessages = new LinkedList<>();
}
