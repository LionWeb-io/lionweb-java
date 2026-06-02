package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.*;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  @Nullable
  DeltaQueryResponse sendQuery(@NotNull Function<String, DeltaQuery> queryProducer);

  /**
   * Commands initiated/requested by the client, with synchronous response by the repository. A
   * command requests some change to the repository. The repository quickly confirms having received
   * the command, or rejects a failed command.[5] However, the repository processes the command
   * asynchronously, and eventually broadcasts the effect(s) as event.
   */
  void sendCommand(
      @NotNull String participationId, @NotNull Function<String, DeltaCommand> commandProducer);

  void sendEvent(@NotNull Function<Integer, DeltaEvent> eventProducer);

  void registerEventReceiver(@NotNull DeltaEventReceiver deltaEventReceiver);

  void unregisterEventReceiver(@NotNull DeltaEventReceiver deltaEventReceiver);

  void registerCommandReceiver(@NotNull DeltaCommandReceiver deltaCommandReceiver);

  void unregisterCommandReceiver(@NotNull DeltaCommandReceiver deltaCommandReceiver);

  void registerQueryReceiver(@NotNull DeltaQueryReceiver deltaQueryReceiver);

  void unregisterQueryReceiver(@NotNull DeltaQueryReceiver deltaQueryReceiver);

  void registerQueryResponseReceiver(
      @NotNull DeltaQueryResponseReceiver deltaQueryResponseReceiver);

  void unregisterQueryResponseReceiver(
      @NotNull DeltaQueryResponseReceiver deltaQueryResponseReceiver);
}
