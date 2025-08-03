package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.client.delta.messages.DeltaCommandResponse;
import io.lionweb.client.delta.messages.DeltaQuery;
import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.client.delta.messages.commands.ChangeProperty;
import io.lionweb.client.delta.messages.events.PropertyAdded;

import java.util.LinkedList;
import java.util.List;

public class MockDeltaChannel implements DeltaChannel {
  List<DeltaCommand> commands = new LinkedList<>();
  int eventSequence = 1;

  private List<DeltaEventReceiver> receivers = new LinkedList<>();

  @Override
  public DeltaQueryResponse sendQuery(DeltaQuery query) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DeltaCommandResponse sendCommand(DeltaCommand command) {
    commands.add(command);

    if (command instanceof ChangeProperty) {
      ChangeProperty changeProperty = (ChangeProperty) command;
      PropertyAdded event = new PropertyAdded(eventSequence++,
              changeProperty.node, changeProperty.property, changeProperty.newValue);
      receivers.forEach(r -> r.receiveEvent(event));
    }

    return new DeltaCommandResponse();
  }

  @Override
  public void registerEventReceiver(DeltaEventReceiver deltaEventReceiver) {
    receivers.add(deltaEventReceiver);
  }

  @Override
  public void unregisterEventReceiver(DeltaEventReceiver deltaEventReceiver) {
    throw new UnsupportedOperationException();
  }
}
