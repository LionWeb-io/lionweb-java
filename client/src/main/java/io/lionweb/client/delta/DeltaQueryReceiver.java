package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaQuery;
import io.lionweb.client.delta.messages.DeltaQueryResponse;

public interface DeltaQueryReceiver {

  DeltaQueryResponse receiveQuery(DeltaQuery query);
}
