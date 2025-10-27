package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaQueryResponse;

public interface DeltaQueryResponseReceiver {

  void receiveQueryResponse(DeltaQueryResponse queryResponse);
}
