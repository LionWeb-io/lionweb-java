package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.*;
import java.util.function.Function;

/**
 * The DeltaChannel must be a specific link between a Client and the Server. Different clients
 * should use different DeltaChannels because the clientId must be determined from the channel.
 */
public interface DeltaChannel {
  /**
   * Queries initiated/requested by the client, with synchronous response by the repository. A query
   * requests some information from the repository without changing the repository’s contents. The
   * repository gathers all information needed to answer the query, and sends the information back.
   * The repository might reply invalid queries with a failure message. We also use queries for
   * managing participations.
   */
  DeltaQueryResponse sendQuery(Function<String, DeltaQuery> queryProducer);

  /**
   * Commands initiated/requested by the client, with synchronous response by the repository. A
   * command requests some change to the repository. The repository quickly confirms having received
   * the command, or rejects a failed command.[5] However, the repository processes the command
   * asynchronously, and eventually broadcasts the effect(s) as event.
   */
  void sendCommand(String participationId, Function<String, DeltaCommand> commandProducer);

  void sendEvent(Function<Integer, DeltaEvent> eventProducer);

  void registerEventReceiver(DeltaEventReceiver deltaEventReceiver);

  void unregisterEventReceiver(DeltaEventReceiver deltaEventReceiver);

  void registerCommandReceiver(DeltaCommandReceiver deltaCommandReceiver);

  void unregisterCommandReceiver(DeltaCommandReceiver deltaCommandReceiver);

  void registerQueryReceiver(DeltaQueryReceiver deltaQueryReceiver);

  void unregisterQueryReceiver(DeltaQueryReceiver deltaQueryReceiver);

  void registerQueryResponseReceiver(DeltaQueryResponseReceiver deltaQueryResponseReceiver);

  void unregisterQueryResponseReceiver(DeltaQueryResponseReceiver deltaQueryResponseReceiver);
}
