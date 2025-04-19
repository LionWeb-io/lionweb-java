package io.lionweb.repoclient;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.repoclient.api.ClassifierKey;
import io.lionweb.repoclient.api.ClassifierResult;
import io.lionweb.repoclient.api.HistorySupport;
import io.lionweb.repoclient.api.RepositoryConfiguration;
import io.lionweb.repoclient.testing.AbstractRepoClientFunctionalTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class LionWebRepoClientHistoryApiFunctionalTest extends AbstractRepoClientFunctionalTest {

  public LionWebRepoClientHistoryApiFunctionalTest() {
    super(LionWebVersion.v2023_1, false);
  }

  @Test
  public void partitionsCRUD() throws IOException {
    String repoName = "myHistoryDB1";
    LionWebRepoClient client =
            new LionWebRepoClient(LionWebVersion.v2023_1, "localhost", getModelRepoPort(), repoName);
    client.createRepository(new RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.ENABLED));
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Create partition
    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesPartition);
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesPartition);
    long v1 = client.createPartitions(client.getJsonSerialization().serializeNodesToJsonString(f1, f2));

    // Delete partitions
    // WAITING FOR https://github.com/LionWeb-io/lionweb-repository/issues/111 to be fixed
    long v2 = client.deletePartitions(Arrays.asList("f1"));

    // Check list
    List<Node> partitionsAt0 = client.historyListPartitions(0);
    assertEquals(0, partitionsAt0.size());

    List<Node> partitionsAt1 = client.historyListPartitions(v1);
    assertEquals(2, partitionsAt1.size());
    assertTrue( partitionsAt1.stream().anyMatch(p -> p.getID().equals("f1")));
    assertTrue( partitionsAt1.stream().anyMatch(p -> p.getID().equals("f2")));

    List<Node> partitionsAt2 = client.historyListPartitions(v2);
    assertEquals(1, partitionsAt2.size());
    assertEquals("f2", partitionsAt2.get(0).getID());
  }
}
