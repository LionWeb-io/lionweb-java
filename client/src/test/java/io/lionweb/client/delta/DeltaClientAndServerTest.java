package io.lionweb.client.delta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.language.Language;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeltaClientAndServerTest {

  @Test
  public void simpleSynchronizationOfNodesInstances() {
      InMemoryServer server = new InMemoryServer();
        server.createRepository(new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));

      JsonSerialization serialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);
      Language language1 = new Language("Language A", "lang-a", "lang-a-key");
      server.createPartition("MyRepo", language1, serialization);

    Language language2 = (Language) server.retrieveAsClassifierInstance("MyRepo", "lang-a", serialization);
    Assertions.assertNotNull(language2);

    assertEquals(language1, language2);

    DeltaChannel channel = new InMemoryDeltaChannel();
    DeltaClient client = new DeltaClient(channel);

    server.monitorDeltaChannel("MyRepo", channel);

    client.monitor(language1);
    client.monitor(language2);

    assertEquals("Language A", language1.getName());
    assertEquals("Language A", language2.getName());

    language1.setName("Language B");
    assertEquals("Language B", language1.getName());
    assertEquals("Language B", language2.getName());

    language2.setName("Language C");
    assertEquals("Language C", language1.getName());
    assertEquals("Language C", language2.getName());

      language1.setName("Language A");
      assertEquals("Language A", language1.getName());
      assertEquals("Language A", language2.getName());
  }
}
