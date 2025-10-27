package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaCommand;

public interface DeltaCommandReceiver {

  void receiveCommand(String participationId, DeltaCommand command);
}
