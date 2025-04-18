package io.lionweb.repoclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.utils.CommonChecks;
import io.lionweb.repoclient.api.HistorySupport;
import io.lionweb.repoclient.api.RepositoryConfiguration;
import io.lionweb.repoclient.testing.AbstractRepoClientFunctionalTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class LionWebRepoClientBulkApiFunctionalTest extends AbstractRepoClientFunctionalTest {

  public LionWebRepoClientBulkApiFunctionalTest() {
    super(LionWebVersion.v2023_1, false);
  }

  @Test
  public void noPartitionsOnNewModelRepository() throws IOException {
    LionWebRepoClient client =
        new LionWebRepoClient(LionWebVersion.v2023_1, "localhost", getModelRepoPort(), "default");
    List<Node> partitions = client.listPartitions();
    assertEquals(Collections.emptyList(), partitions);
  }

  @Test
  public void partitionsCRUD() throws IOException {
    LionWebRepoClient client =
        new LionWebRepoClient(LionWebVersion.v2023_1, "localhost", getModelRepoPort(), "default");
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Create partition
    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesPartition);
    client.createPartitions(client.getJsonSerialization().serializeNodesToJsonString(f1));

    // Check list
    List<Node> nodes1 = client.listPartitions();
    assertEquals(1, nodes1.size());
    assertEquals("f1", nodes1.get(0).getID());
    assertEquals(PropertiesLanguage.propertiesPartition, nodes1.get(0).getClassifier());
    assertEquals(Collections.singletonList("f1"), client.listPartitionsIDs());

    // Create partitions
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesPartition);
    DynamicNode f3 = new DynamicNode("f3", PropertiesLanguage.propertiesPartition);
    client.createPartitions(client.getJsonSerialization().serializeNodesToJsonString(f2, f3));

    // Check list
    List<Node> nodes2 = client.listPartitions();
    assertEquals(3, nodes2.size());
    assertEquals(
        new HashSet<>(Arrays.asList("f1", "f2", "f3")), new HashSet<>(client.listPartitionsIDs()));

    // Delete partitions
    client.deletePartitions(Arrays.asList("f1", "f3"));

    // Check list
    List<Node> nodes3 = client.listPartitions();
    assertEquals(1, nodes3.size());
    assertEquals("f2", nodes3.get(0).getID());
    assertEquals(PropertiesLanguage.propertiesPartition, nodes3.get(0).getClassifier());
    assertEquals(Collections.singletonList("f2"), client.listPartitionsIDs());

    // Delete partition
    client.deletePartitions(Collections.singletonList("f2"));

    // Check list
    List<Node> nodes4 = client.listPartitions();
    assertEquals(0, nodes4.size());
    assertEquals(Collections.emptyList(), client.listPartitionsIDs());
  }

  @Test
  public void storeOnCustomRepository() throws IOException {
    String repoName = "my_repo";
    LionWebRepoClient client =
            new LionWebRepoClient(LionWebVersion.v2023_1, "localhost", getModelRepoPort(), repoName);
    client.createRepository(new RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED));
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Create partition
    DynamicNode partition = new DynamicNode("partition", PropertiesLanguage.propertiesPartition);
    client.createPartitions(client.getJsonSerialization().serializeNodesToJsonString(partition));

    // Check list
    List<Node> nodes1 = client.listPartitions();
    assertEquals(1, nodes1.size());
    assertEquals("partition", nodes1.get(0).getID());
    assertEquals(PropertiesLanguage.propertiesPartition, nodes1.get(0).getClassifier());
    assertEquals(Collections.singletonList("partition"), client.listPartitionsIDs());

    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.addChild(partition, "files", f1);

    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.addChild(partition, "files", f2);

    client.store(partition);

    Node retrievedPartition = client.retrieve(partition.getID());
    assertEquals(partition, retrievedPartition);
  }

  @Test
  public void ids() throws IOException {
    LionWebRepoClient client =
        new LionWebRepoClient(LionWebVersion.v2023_1, "localhost", getModelRepoPort(), "default");

    List<String> ids1 = client.ids(78);
    assertEquals(78, ids1.size());
    assertTrue(ids1.stream().allMatch(CommonChecks::isValidID));

    List<String> ids2 = client.ids(0);
    assertEquals(0, ids2.size());

    List<String> ids3 = client.ids(1);
    assertEquals(1, ids3.size());
    assertTrue(ids3.stream().allMatch(CommonChecks::isValidID));
  }

  @Test
  public void storeAndRetrieve() throws IOException {
    LionWebRepoClient client =
        new LionWebRepoClient(LionWebVersion.v2023_1, "localhost", getModelRepoPort(), "default");
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.createPartitions(client.getJsonSerialization().serializeNodesToJsonString(p1));

    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt");
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt");
    ClassifierInstanceUtils.addChild(p1, "files", f1);
    ClassifierInstanceUtils.addChild(p1, "files", f2);

    client.store(Collections.singletonList(p1));

    List<Node> retrievedNodes1 = client.retrieve(Collections.singletonList("p1"), 10);
    assertEquals(1, retrievedNodes1.size());
    assertEquals(p1, retrievedNodes1.get(0));
  }
}
