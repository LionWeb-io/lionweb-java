package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQueryResponse;
import org.jetbrains.annotations.NotNull;

/** Acknowledgement response for an InformAboutChangingPartitionsRequest. */
public class InformAboutChangingPartitionsResponse extends DeltaQueryResponse {

  public InformAboutChangingPartitionsResponse(@NotNull String queryId) {
    super(queryId);
  }

  @Override
  public String toString() {
    return "InformAboutChangingPartitionsResponse{" + "queryId='" + queryId + '\'' + '}';
  }
}
