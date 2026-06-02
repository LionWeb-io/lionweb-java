package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaCommand;
import org.jetbrains.annotations.NotNull;

public interface DeltaCommandReceiver {

  void receiveCommand(@NotNull String participationId, @NotNull DeltaCommand command);
}
