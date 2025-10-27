package io.lionweb.client.delta;

import static org.junit.Assert.*;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.delta.messages.events.StandardErrorCode;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.utils.ModelComparator;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeltaClientAndServerTest {

  @Test
  public void simpleSynchronizationOfNodesInstances() {
    InMemoryServer server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));

    JsonSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);
    Language language1 = new Language("Language A", "lang-a", "lang-a-key");
    server.createPartition("MyRepo", language1, serialization);

    Language language2 =
        (Language) server.retrieveAsClassifierInstance("MyRepo", "lang-a", serialization);
    Assertions.assertNotNull(language2);

    assertEquals(language1, language2);

    DeltaChannel channel = new InMemoryDeltaChannel();
    server.monitorDeltaChannel("MyRepo", channel);

    DeltaClient client1 = new DeltaClient(channel, "my-client-1");
    client1.sendSignOnRequest();
    client1.monitor(language1);
    DeltaClient client2 = new DeltaClient(channel, "my-client-2");
    client2.sendSignOnRequest();
    client2.monitor(language2);

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

  @Test
  public void changingUnexistingNodeCauseError() {
    InMemoryServer server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));

    JsonSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);
    Language language1 = new Language("Language A", "lang-a", "lang-a-key");
    // We do NOT create the partition on the repository

    DeltaChannel channel = new InMemoryDeltaChannel();
    server.monitorDeltaChannel("MyRepo", channel);

    DeltaClient client = new DeltaClient(channel, "my-client-1");
    client.sendSignOnRequest();

    client.monitor(language1);
    try {
      language1.setName("Language B");
    } catch (ErrorEventReceivedException e) {
      assertEquals(StandardErrorCode.UNKNOWN_NODE.code, e.getCode());
      assertEquals("Node with id lang-a not found", e.getErrorMessage());
      return;
    }
    fail("Expected exception not thrown");
  }

  @Test
  public void addingChildren() {
    InMemoryServer server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));

    JsonSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);
    Language language1 = new Language("Language A", "lang-a", "lang-a-key");
    server.createPartition("MyRepo", language1, serialization);

    Language language2 =
        (Language) server.retrieveAsClassifierInstance("MyRepo", "lang-a", serialization);
    Assertions.assertNotNull(language2);

    assertEquals(language1, language2);

    DeltaChannel channel = new InMemoryDeltaChannel();
    server.monitorDeltaChannel("MyRepo", channel);

    DeltaClient client1 = new DeltaClient(channel, "my-client-1");
    client1.sendSignOnRequest();

    DeltaClient client2 = new DeltaClient(channel, "my-client-2");
    client2.sendSignOnRequest();

    client1.monitor(language1);
    client2.monitor(language2);

    assertEquals(Collections.emptyList(), language1.getElements());
    assertEquals(Collections.emptyList(), language2.getElements());

    Concept concept1 = new Concept(language1, "Concept A", "concept-a", "a");
    language1.addElement(concept1);
    assertTrue(
        ModelComparator.areEquivalent(
            Collections.singletonList(concept1), language1.getElements()));
    assertTrue(
        ModelComparator.areEquivalent(
            Collections.singletonList(concept1), language2.getElements()));
  }
}
