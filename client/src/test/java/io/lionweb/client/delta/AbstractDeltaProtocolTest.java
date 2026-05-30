package io.lionweb.client.delta;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.InMemoryServer;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDeltaProtocolTest {

  protected @NotNull InMemoryServer createServerWithRepository() {
    InMemoryServer server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));
    return server;
  }

  protected @NotNull DeltaChannel prepareChannel(@NotNull InMemoryServer server) {
    DeltaChannel channel = new InMemoryDeltaChannel();
    server.monitorDeltaChannel("MyRepo", channel);
    return channel;
  }
}
