package io.lionweb.client.partitioned;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Mirrors {@code NodesLevelInMemoryServerClientTest} for the partitioned implementation. */
public class NodesLevelPartitionedServerClientTest {

  @TempDir Path tempDir;

  @Test
  public void testRepositoriesCRUD() {
    try (PartitionedServer server = new PartitionedServer(tempDir)) {
      NodesLevelPartitionedServerClient client = new NodesLevelPartitionedServerClient(server);
      assertEquals(Collections.emptySet(), client.listRepositories());

      client.createRepository(
          new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));
      assertEquals(
          Collections.singleton(
              new RepositoryConfiguration(
                  "MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED)),
          client.listRepositories());

      client.deleteRepository("MyRepo");
      assertEquals(Collections.emptySet(), client.listRepositories());
    }
  }

  @Test
  public void testPartitionsCRUD() throws Exception {
    try (PartitionedServer server = new PartitionedServer(tempDir)) {
      NodesLevelPartitionedServerClient client =
          new NodesLevelPartitionedServerClient(server, "MyRepo");
      client.createRepository(
          new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));

      assertEquals(Collections.emptyList(), client.listPartitions());

      Language l1 =
          new Language(LionWebVersion.v2024_1, "MyLanguage")
              .setID("l-id")
              .setKey("l-key")
              .setVersion("1.0");
      Concept c1 = new Concept(l1, "MyConcept", "c1-id").setKey("c1-key");

      client.createPartitions(Collections.singletonList(l1));
      assertEquals(Collections.singletonList(l1), client.listPartitions());

      client.deletePartitions(Collections.singletonList("l-id"));
      assertEquals(Collections.emptyList(), client.listPartitions());
    }
  }

  @Test
  public void testNodesModification() throws Exception {
    try (PartitionedServer server = new PartitionedServer(tempDir)) {
      NodesLevelPartitionedServerClient client =
          new NodesLevelPartitionedServerClient(server, "MyRepo");
      client.createRepository(
          new RepositoryConfiguration("MyRepo", LionWebVersion.v2024_1, HistorySupport.DISABLED));

      Language l1 =
          new Language(LionWebVersion.v2024_1, "MyLanguage")
              .setID("l-id")
              .setKey("l-key")
              .setVersion("1.0");
      Concept c1 = new Concept(l1, "MyConcept", "c1-id").setKey("c1-key");

      client.createPartitions(Collections.singletonList(l1));
      assertEquals(Collections.singletonList(l1), client.listPartitions());
      assertEquals(c1, client.retrieve(Collections.singletonList("c1-id")).get(0));
    }
  }
}
