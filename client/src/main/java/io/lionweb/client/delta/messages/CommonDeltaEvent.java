package io.lionweb.client.delta.messages;

import io.lionweb.client.delta.CommandSource;
import java.util.LinkedList;
import java.util.List;

public class CommonDeltaEvent extends DeltaEvent {
  public final int sequenceNumber;
  public final List<CommandSource> originCommands = new LinkedList<>();

  public CommonDeltaEvent(int sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public void addSource(CommandSource source) {
    originCommands.add(source);
  }
}
