package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaCommand;
import org.jetbrains.annotations.NotNull;

/**
 * Handles incoming {@link io.lionweb.client.delta.messages.DeltaCommand} messages from a
 * participation.
 */
public interface DeltaCommandReceiver {

  void receiveCommand(@NotNull String participationId, @NotNull DeltaCommand command);
}
