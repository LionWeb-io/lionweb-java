package io.lionweb.client.delta;

import io.lionweb.model.Node;

public class MockDeltaSynchronizer extends DeltaSynchronizer {
  public MockDeltaSynchronizer(DeltaChannel channel) {
    super(channel);
  }

  @Override
  protected void forceState(Node node) {
    throw new UnsupportedOperationException();
  }
}
