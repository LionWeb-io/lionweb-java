package io.lionweb.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.client.languages.PropertiesLanguage;
import io.lionweb.client.testing.AbstractClientFunctionalTest;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.model.impl.DynamicNode;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class LionWebClientHistoryApiFunctionalTest extends AbstractClientFunctionalTest {

  public LionWebClientHistoryApiFunctionalTest() {
    super(LionWebVersion.v2023_1, false);
  }

  @Test
  public void partitionsCRUD() throws IOException {
    String repoName = "myHistoryDB1";
    LionWebClient client =
        new LionWebClient(LionWebVersion.v2023_1, "localhost", getServerPort(), repoName);
    client.createRepository(
        new RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.ENABLED));
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Create partition
    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesPartition);
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesPartition);
    RepositoryVersionToken v1 =
        client.rawCreatePartitions(
            client.getJsonSerialization().serializeNodesToJsonString(f1, f2));

    // Delete partitions
    RepositoryVersionToken v2 = client.deletePartitions(Arrays.asList("f1"));

    // Check list
    RepositoryVersionToken v0 = new RepositoryVersionToken("0");
    List<Node> partitionsAt0 = client.listPartitions(v0);
    assertEquals(0, partitionsAt0.size());

    List<Node> partitionsAt1 = client.listPartitions(v1);
    assertEquals(2, partitionsAt1.size());
    assertTrue(partitionsAt1.stream().anyMatch(p -> p.getID().equals("f1")));
    assertTrue(partitionsAt1.stream().anyMatch(p -> p.getID().equals("f2")));

    List<Node> partitionsAt2 = client.listPartitions(v2);
    assertEquals(1, partitionsAt2.size());
    assertEquals("f2", partitionsAt2.get(0).getID());
  }

  @Test
  public void partitionHistory() throws IOException {
    String repoName = "myHistoryDB2";
    LionWebClient client =
        new LionWebClient(LionWebVersion.v2023_1, "localhost", getServerPort(), repoName);
    client.createRepository(
        new RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.ENABLED));
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Create partition, initially empty
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    RepositoryVersionToken v0 =
        client.rawCreatePartitions(client.getJsonSerialization().serializeNodesToJsonString(p1));

    // Populate partition
    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "a/b/c");
    ClassifierInstanceUtils.addChild(p1, "files", f1);
    RepositoryVersionToken v1 = client.store(p1);

    // Modify property
    ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "a/b/foo");
    RepositoryVersionToken v2 = client.store(p1);

    // Add child
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "a/b/c2");
    ClassifierInstanceUtils.addChild(p1, "files", f2);
    RepositoryVersionToken v3 = client.store(p1);

    // Delete child
    p1.removeChild(f1);
    RepositoryVersionToken v4 = client.store(p1);

    // Check data
    Node p1_v0 = client.retrieve(v0, p1.getID());
    assertEquals(0, ClassifierInstanceUtils.getChildrenByContainmentName(p1_v0, "files").size());

    Node p1_v1 = client.retrieve(v1, p1.getID());
    assertEquals(1, ClassifierInstanceUtils.getChildrenByContainmentName(p1_v1, "files").size());
    Node f1_v1 = ClassifierInstanceUtils.getChildrenByContainmentName(p1_v1, "files").get(0);
    assertEquals("a/b/c", ClassifierInstanceUtils.getPropertyValueByName(f1_v1, "path"));

    Node p1_v2 = client.retrieve(v2, p1.getID());
    assertEquals(1, ClassifierInstanceUtils.getChildrenByContainmentName(p1_v2, "files").size());
    Node f1_v2 = ClassifierInstanceUtils.getChildrenByContainmentName(p1_v2, "files").get(0);
    assertEquals("a/b/foo", ClassifierInstanceUtils.getPropertyValueByName(f1_v2, "path"));

    Node p1_v3 = client.retrieve(v3, p1.getID());
    assertEquals(2, ClassifierInstanceUtils.getChildrenByContainmentName(p1_v3, "files").size());
    Node f1_v3 = ClassifierInstanceUtils.getChildrenByContainmentName(p1_v3, "files").get(0);
    assertEquals("a/b/foo", ClassifierInstanceUtils.getPropertyValueByName(f1_v3, "path"));
    Node f2_v3 = ClassifierInstanceUtils.getChildrenByContainmentName(p1_v3, "files").get(1);
    assertEquals("a/b/c2", ClassifierInstanceUtils.getPropertyValueByName(f2_v3, "path"));

    Node p1_v4 = client.retrieve(v4, p1.getID());
    assertEquals(1, ClassifierInstanceUtils.getChildrenByContainmentName(p1_v4, "files").size());
    Node f2_v4 = ClassifierInstanceUtils.getChildrenByContainmentName(p1_v4, "files").get(0);
    assertEquals("a/b/c2", ClassifierInstanceUtils.getPropertyValueByName(f2_v4, "path"));
  }
}
