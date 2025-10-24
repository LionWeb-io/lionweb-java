package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.client.delta.messages.DeltaCommandResponse;
import io.lionweb.client.delta.messages.DeltaEvent;

public interface DeltaCommandReceiver {

  DeltaCommandResponse receiveCommand(DeltaCommand command);
}
