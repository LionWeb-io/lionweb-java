package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base for events broadcast by the server to subscribed clients in the LionWeb Delta
 * protocol.
 *
 * @see <a href="https://lionweb.io/specification/delta/delta-api.html">LionWeb Delta API
 *     specification</a>
 */
public abstract class DeltaEvent {
  /**
   * Represents additional information associated with a protocol message in the Delta framework.
   */
  public final List<AdditionalInfo> additionalInfos = new LinkedList<>();
}
