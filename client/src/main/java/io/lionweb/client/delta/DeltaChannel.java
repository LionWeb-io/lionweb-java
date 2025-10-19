package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.client.delta.messages.DeltaCommandResponse;
import io.lionweb.client.delta.messages.DeltaQuery;
import io.lionweb.client.delta.messages.DeltaQueryResponse;

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
  DeltaQueryResponse sendQuery(DeltaQuery query);

  /**
   * Commands initiated/requested by the client, with synchronous response by the repository. A
   * command requests some change to the repository. The repository quickly confirms having received
   * the command, or rejects a failed command.[5] However, the repository processes the command
   * asynchronously, and eventually broadcasts the effect(s) as event.
   */
  DeltaCommandResponse sendCommand(DeltaCommand command);

  void registerEventReceiver(DeltaEventReceiver deltaEventReceiver);

  void unregisterEventReceiver(DeltaEventReceiver deltaEventReceiver);
}
