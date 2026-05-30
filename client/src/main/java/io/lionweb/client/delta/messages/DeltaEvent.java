package io.lionweb.client.delta.messages;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents an abstract base class for events in the delta framework. DeltaEvent serves as a
 * foundation for specific types of delta events that may involve communication via protocol
 * messages.
 */
public abstract class DeltaEvent {
  /**
   * Represents additional information associated with a protocol message in the Delta framework.
   */
  public final List<AdditionalInfo> additionalInfos = new LinkedList<>();
}
