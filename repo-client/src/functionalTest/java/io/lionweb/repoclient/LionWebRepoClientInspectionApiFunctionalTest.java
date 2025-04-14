package io.lionweb.repoclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.repoclient.api.ClassifierKey;
import io.lionweb.repoclient.api.ClassifierResult;
import io.lionweb.repoclient.api.HistorySupport;
import io.lionweb.repoclient.api.RepositoryConfiguration;
import io.lionweb.repoclient.testing.AbstractRepoClientFunctionalTest;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class LionWebRepoClientInspectionApiFunctionalTest extends AbstractRepoClientFunctionalTest {

  public LionWebRepoClientInspectionApiFunctionalTest() {
    super(LionWebVersion.v2023_1, false);
  }

  @Test
  public void nodesByClassifier() throws IOException {
    LionWebRepoClient client =
        new LionWebRepoClient(
            LionWebVersion.v2023_1, "localhost", getModelRepoPort(), "repo_nodesByClassifier");
    client.createRepository(
        new RepositoryConfiguration(
            "repo_nodesByClassifier", LionWebVersion.v2023_1, HistorySupport.Disabled));
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Get data with empty repository
    Map<ClassifierKey, ClassifierResult> res1 = client.nodesByClassifier();
    assertEquals(Collections.emptyMap(), res1);

    // Add nodes
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.createPartitions(client.getJsonSerialization().serializeNodesToJsonString(p1));

    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt");
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt");
    ClassifierInstanceUtils.addChild(p1, "files", f1);
    ClassifierInstanceUtils.addChild(p1, "files", f2);
    client.store(Collections.singletonList(p1));

    // Get data after insertion
    Map<ClassifierKey, ClassifierResult> res2 = client.nodesByClassifier();
    Map<ClassifierKey, ClassifierResult> exp2 = new HashMap<>();
    exp2.put(
        new ClassifierKey(
            PropertiesLanguage.propertiesLanguage.getKey(),
            PropertiesLanguage.propertiesPartition.getKey()),
        new ClassifierResult(new HashSet<>(Arrays.asList("p1")), 1));
    exp2.put(
        new ClassifierKey(
            PropertiesLanguage.propertiesLanguage.getKey(),
            PropertiesLanguage.propertiesFile.getKey()),
        new ClassifierResult(new HashSet<>(Arrays.asList("f1", "f2")), 2));
    assertEquals(exp2, res2);
  }

  @Test
  public void nodesByLanguage() throws IOException {
    LionWebRepoClient client =
        new LionWebRepoClient(
            LionWebVersion.v2023_1, "localhost", getModelRepoPort(), "repo_nodesByLanguage");
    client.createRepository(
        new RepositoryConfiguration(
            "repo_nodesByLanguage", LionWebVersion.v2023_1, HistorySupport.Disabled));
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Get data with empty repository
    Map<String, ClassifierResult> res1 = client.nodesByLanguage();
    assertEquals(Collections.emptyMap(), res1);

    // Add nodes
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.createPartitions(client.getJsonSerialization().serializeNodesToJsonString(p1));

    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt");
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt");
    ClassifierInstanceUtils.addChild(p1, "files", f1);
    ClassifierInstanceUtils.addChild(p1, "files", f2);
    client.store(Collections.singletonList(p1));

    // Get data after insertion
    Map<String, ClassifierResult> res2 = client.nodesByLanguage();
    Map<String, ClassifierResult> exp2 = new HashMap<>();
    exp2.put(
        PropertiesLanguage.propertiesLanguage.getKey(),
        new ClassifierResult(new HashSet<>(Arrays.asList("p1", "f1", "f2")), 3));
    assertEquals(exp2, res2);
  }
}
