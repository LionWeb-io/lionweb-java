package io.lionweb.client.delta.messages;

import io.lionweb.client.delta.CommandSource;
import java.util.LinkedList;
import java.util.List;

/** Base class used by most delta events. */
public abstract class BaseDeltaEvent<T extends BaseDeltaEvent<?>> extends DeltaEvent {
  public final int sequenceNumber;
  public final List<CommandSource> originCommands = new LinkedList<>();

  public BaseDeltaEvent(int sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public T addSource(CommandSource source) {
    originCommands.add(source);
    return (T) this;
  }
}
