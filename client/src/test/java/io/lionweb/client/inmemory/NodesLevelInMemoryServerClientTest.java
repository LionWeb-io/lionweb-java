package io.lionweb.client.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class NodesLevelInMemoryServerClientTest {

  @Test
  public void testRepositoriesCRUD() {
    InMemoryServer server = new InMemoryServer();
    NodesLevelInMemoryServerClient client = new NodesLevelInMemoryServerClient(server);
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
    NodesLevelInMemoryServerClient client = new NodesLevelInMemoryServerClient(server, "MyRepo");
    client.createRepository(
        new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));

    assertEquals(Collections.emptyList(), client.listPartitions());

    Language l1 =
        new Language(LionWebVersion.v2024_1, "MyLanguage")
            .setID("l-id")
            .setKey("l-key")
            .setVersion("1.0");
    Concept c1 = new Concept(l1, "MyConcept").setID("c1-id").setKey("c1-key");

    client.createPartitions(Collections.singletonList(l1));
    assertEquals(Collections.singletonList(l1), client.listPartitions());

    client.deletePartitions(Collections.singletonList("l-id"));
    assertEquals(Collections.emptyList(), client.listPartitions());
  }

  @Test
  public void testNodesModification() {
    InMemoryServer server = new InMemoryServer();
    NodesLevelInMemoryServerClient client = new NodesLevelInMemoryServerClient(server, "MyRepo");
    client.createRepository(
        new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));
    Language l1 =
        new Language(LionWebVersion.v2024_1, "MyLanguage")
            .setID("l-id")
            .setKey("l-key")
            .setVersion("1.0");
    Concept c1 = new Concept(l1, "MyConcept").setID("c1-id").setKey("c1-key");

    client.createPartitions(Collections.singletonList(l1));
    assertEquals(Collections.singletonList(l1), client.listPartitions());
    assertEquals(c1, client.retrieve(Collections.singletonList("c1-id")).get(0));
  }
}
