package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class InMemoryDeltaChannel implements DeltaChannel {
  private final Set<DeltaEventReceiver> eventReceivers = new HashSet<>();
  private @Nullable DeltaCommandReceiver commandReceiver;
  private int nextEventId = 1;
  private int nextCommandId = 1;

  @Override
  public DeltaQueryResponse sendQuery(DeltaQuery query) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void sendCommand(Function<String, DeltaCommand> commandProducer) {
    if (commandReceiver != null) {
      commandReceiver.receiveCommand(commandProducer.apply("cmd-" + nextCommandId++));
    }
  }

  @Override
  public void registerEventReceiver(DeltaEventReceiver deltaEventReceiver) {
    eventReceivers.add(deltaEventReceiver);
  }

  @Override
  public void unregisterEventReceiver(DeltaEventReceiver deltaEventReceiver) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void registerCommandReceiver(DeltaCommandReceiver commandReceiver) {
    this.commandReceiver = commandReceiver;
  }

  @Override
  public void unregisterCommandReceiver(DeltaCommandReceiver deltaCommandReceiver) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void sendEvent(Function<Integer, DeltaEvent> eventProducer) {
    eventReceivers.forEach(receiver -> receiver.receiveEvent(eventProducer.apply(nextEventId++)));
  }
}
