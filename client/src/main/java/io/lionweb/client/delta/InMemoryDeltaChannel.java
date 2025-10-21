package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class InMemoryDeltaChannel implements DeltaChannel {
  private final Set<DeltaEventReceiver> receivers = new HashSet<>();
  private int nextCmdId = 1;

  @Override
  public DeltaQueryResponse sendQuery(DeltaQuery query) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public DeltaCommandResponse sendCommand(Function<String, DeltaCommand> commandProducer) {
    //        String commandId = "cmd-" + nextCmdId++;
    //        DeltaCommand command = commandProducer.apply(commandId);
    //        receivers.forEach(receiver -> receiver.);
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void registerEventReceiver(DeltaEventReceiver deltaEventReceiver) {
    receivers.add(deltaEventReceiver);
  }

  @Override
  public void unregisterEventReceiver(DeltaEventReceiver deltaEventReceiver) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void sendEvent(Function<Integer, DeltaEvent> eventProducer) {
    receivers.forEach(receiver -> receiver.receiveEvent(eventProducer.apply(nextCmdId++)));
  }
}
