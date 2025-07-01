package io.lionweb.serverclient.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.ChunkLevelInMemoryServerClient;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.serialization.AbstractSerialization;
import io.lionweb.serialization.SerializationProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  public void testPartitionsCRUD() throws IOException {
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

    AbstractSerialization serialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);

    client.createPartitionsFromChunk(serialization.serializeNodesToSerializationChunk(l1).getClassifierInstances());
    assertEquals(Collections.singletonList("l-id"), client.listPartitionsIDs());

    client.deletePartitions(Collections.singletonList("l-id"));
    assertEquals(Collections.emptyList(), client.listPartitionsIDs());
  }

//  @Test
//  public void testNodesModification() throws IOException {
//    InMemoryServer server = new InMemoryServer();
//    ChunkLevelInMemoryServerClient client = new ChunkLevelInMemoryServerClient(server);
//    client.createRepository(
//            new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));
//    client.setRepositoryName("MyRepo");
//    Language l1 =
//            new Language(LionWebVersion.v2024_1, "MyLanguage")
//                    .setID("l-id")
//                    .setKey("l-key")
//                    .setVersion("1.0");
//    Concept c1 = new Concept(l1, "MyConcept").setID("c1-id").setKey("c1-key");
//
//    client.createPartitions(Collections.singletonList(l1));
//    assertEquals(Collections.singletonList(l1), client.listPartitions());
//    assertEquals(c1, client.retrieve(Collections.singletonList("c1-id")).get(0));
//
//  }

}
