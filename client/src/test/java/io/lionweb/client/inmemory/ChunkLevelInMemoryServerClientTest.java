package io.lionweb.client.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.serialization.AbstractSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializedChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.Test;

public class ChunkLevelInMemoryServerClientTest {

  @Test
  public void testRepositoriesCRUD() {
    InMemoryServer server = new InMemoryServer();
    ChunkLevelInMemoryServerClient client = new ChunkLevelInMemoryServerClient(server);
    assertEquals(Collections.emptySet(), client.listRepositories());

    client.createRepository(
        new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));
    assertEquals(
        Collections.singleton(
            new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED)),
        client.listRepositories());

    client.deleteRepository("MyRepo");
    assertEquals(Collections.emptySet(), client.listRepositories());
  }

  @Test
  public void testPartitionsCRUD() {
    InMemoryServer server = new InMemoryServer();
    ChunkLevelInMemoryServerClient client = new ChunkLevelInMemoryServerClient(server);
    client.createRepository(
        new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));
    client.setRepositoryName("MyRepo");

    assertEquals(Collections.emptyList(), client.listPartitionsIDs());

    Language l1 =
        new Language(LionWebVersion.v2024_1, "MyLanguage")
            .setID("l-id")
            .setKey("l-key")
            .setVersion("1.0");
    Concept c1 = new Concept(l1, "MyConcept").setID("c1-id").setKey("c1-key");

    AbstractSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);

    client.createPartitionsFromChunk(
        serialization.serializeTreeToSerializationChunk(l1).getClassifierInstances());
    assertEquals(Collections.singletonList("l-id"), client.listPartitionsIDs());

    client.deletePartitions(Collections.singletonList("l-id"));
    assertEquals(Collections.emptyList(), client.listPartitionsIDs());
  }

  @Test
  public void testNodesModification() throws IOException {
    InMemoryServer server = new InMemoryServer();
    ChunkLevelInMemoryServerClient client = new ChunkLevelInMemoryServerClient(server);
    client.createRepository(
        new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));
    client.setRepositoryName("MyRepo");
    Language l1 =
        new Language(LionWebVersion.v2024_1, "MyLanguage")
            .setID("l-id")
            .setKey("l-key")
            .setVersion("1.0");
    Concept c1 = new Concept(l1, "MyConcept").setID("c1-id").setKey("c1-key");

    AbstractSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);

    SerializedChunk chunk = serialization.serializeTreeToSerializationChunk(l1);
    client.createPartitionsFromChunk(chunk.getClassifierInstances());
    assertEquals(Collections.singletonList("l-id"), client.listPartitionsIDs());
    List<SerializedClassifierInstance> retrievedSerializedNodes =
        client.retrieveAsChunk(Collections.singletonList("c1-id"));
    SerializedChunk serializedChunk =
        SerializedChunk.fromNodes(LionWebVersion.v2024_1, retrievedSerializedNodes);
    SerializedChunk serializedC1 = serialization.serializeTreeToSerializationChunk(c1);
    assertEquals(serializedC1, serializedChunk);
  }
}
