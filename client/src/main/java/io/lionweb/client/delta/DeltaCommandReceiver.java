package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.client.delta.messages.DeltaCommandResponse;

public interface DeltaCommandReceiver {

  DeltaCommandResponse receiveCommand(DeltaCommand command);
}
