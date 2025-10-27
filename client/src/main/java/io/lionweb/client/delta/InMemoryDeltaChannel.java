package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.*;
import java.util.*;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InMemoryDeltaChannel implements DeltaChannel {
  private final Set<DeltaEventReceiver> eventReceivers = new HashSet<>();
  private @Nullable DeltaCommandReceiver commandReceiver;
  private @Nullable DeltaQueryReceiver queryReceiver;
  private @Nullable List<DeltaQueryResponseReceiver> queryResponseReceivers = new ArrayList<>();
  private int nextEventId = 1;
  private int nextCommandId = 1;
  private int nextQueryId = 1;

  @Override
  public DeltaQueryResponse sendQuery(Function<String, DeltaQuery> queryProducer) {
    if (queryReceiver != null) {
      DeltaQueryResponse response =
          queryReceiver.receiveQuery(queryProducer.apply("query-" + nextQueryId++));
      queryResponseReceivers.forEach(receiver -> receiver.receiveQueryResponse(response));
      return response;
    }

    return null;
  }

  @Override
  public void sendCommand(
      @NotNull String participationId, Function<String, DeltaCommand> commandProducer) {
    Objects.requireNonNull(participationId, "participationId must not be null");
    if (commandReceiver != null) {
      commandReceiver.receiveCommand(
          participationId, commandProducer.apply("cmd-" + nextCommandId++));
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

  @Override
  public void registerQueryReceiver(DeltaQueryReceiver deltaQueryReceiver) {
    this.queryReceiver = deltaQueryReceiver;
  }

  @Override
  public void unregisterQueryReceiver(DeltaQueryReceiver deltaQueryReceiver) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void registerQueryResponseReceiver(DeltaQueryResponseReceiver deltaQueryResponseReceiver) {
    this.queryResponseReceivers.add(deltaQueryResponseReceiver);
  }

  @Override
  public void unregisterQueryResponseReceiver(
      DeltaQueryResponseReceiver deltaQueryResponseReceiver) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
