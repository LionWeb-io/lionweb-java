package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.client.delta.messages.DeltaCommandResponse;
import io.lionweb.client.delta.messages.DeltaQuery;
import io.lionweb.client.delta.messages.DeltaQueryResponse;
import io.lionweb.client.delta.messages.commands.ChangeProperty;
import io.lionweb.client.delta.messages.events.PropertyAdded;
import io.lionweb.client.delta.messages.queries.GetAvailableIds;
import io.lionweb.client.delta.messages.queries.SignOff;
import io.lionweb.client.delta.messages.queries.SignOn;
import java.util.*;

public class MockDeltaChannel implements DeltaChannel {
  List<DeltaQuery> queries = new LinkedList<>();
  List<DeltaCommand> commands = new LinkedList<>();
  int eventSequence = 1;
  int participationsCounter = 1;
  // Each Channel is for the communication between one client and one server
  // so we have one clientId. Also, each client can have one participation
  // active, so we have one participationid.
  String clientId;
  String participationId;
  int nextId = 1;

  private List<DeltaEventReceiver> receivers = new LinkedList<>();

  @Override
  public DeltaQueryResponse sendQuery(DeltaQuery query) {
    queries.add(query);
    if (query instanceof SignOn) {
      SignOn signOn = (SignOn) query;
      DeltaQueryResponse response = new DeltaQueryResponse(query.queryId);
      String participationId = "p-" + participationsCounter++;
      this.clientId = signOn.clientId;
      this.participationId = participationId;
      response.values.put("participationId", participationId);
      return response;
    } else if (query instanceof SignOff) {
      SignOff signOff = (SignOff) query;
      DeltaQueryResponse response = new DeltaQueryResponse(query.queryId);
      this.participationId = null;
      return response;
    } else if (query instanceof GetAvailableIds) {
      GetAvailableIds getAvailableIds = (GetAvailableIds) query;
      DeltaQueryResponse response = new DeltaQueryResponse(query.queryId);
      List<String> ids = new ArrayList<>();
      for (int i = 0; i < getAvailableIds.count; i++) {
        ids.add("id-" + nextId++);
      }
      response.values.put("ids", ids);
      return response;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public DeltaCommandResponse sendCommand(DeltaCommand command) {
    commands.add(command);

    if (command instanceof ChangeProperty) {
      ChangeProperty changeProperty = (ChangeProperty) command;
      PropertyAdded event =
          new PropertyAdded(
              eventSequence++,
              changeProperty.node,
              changeProperty.property,
              changeProperty.newValue);
      event.addSource(new CommandSource(participationId, command.commandId));
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
