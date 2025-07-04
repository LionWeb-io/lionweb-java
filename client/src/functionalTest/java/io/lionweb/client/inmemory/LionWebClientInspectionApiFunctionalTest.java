package io.lionweb.client.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.ClassifierKey;
import io.lionweb.client.api.ClassifierResult;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.languages.PropertiesLanguage;
import io.lionweb.client.testing.AbstractClientInMemoryFunctionalTest;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.impl.DynamicNode;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class LionWebClientInspectionApiFunctionalTest extends AbstractClientInMemoryFunctionalTest {

  public LionWebClientInspectionApiFunctionalTest() {
    super(LionWebVersion.v2023_1);
  }

  @Test
  public void nodesByClassifier() throws IOException {
    String repoName = "repo_nodesByClassifier";
    NodesLevelInMemoryServerClient client =
        new NodesLevelInMemoryServerClient(getServer(), repoName);
    client.createRepository(
        new RepositoryConfiguration(
            "repo_nodesByClassifier", LionWebVersion.v2023_1, HistorySupport.DISABLED));

    // Get data with empty repository
    Map<ClassifierKey, ClassifierResult> res1 = client.nodesByClassifier();
    assertEquals(Collections.emptyMap(), res1);

    // Add nodes
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.createPartitions(p1);

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
  public void nodesByClassifierWithLimit() throws IOException {
    String repoName = "repo_nodesByClassifierWithLimit";
    NodesLevelInMemoryServerClient client =
        new NodesLevelInMemoryServerClient(getServer(), repoName);
    client.createRepository(
        new RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED));

    // Get data with empty repository
    Map<ClassifierKey, ClassifierResult> res1 = client.nodesByClassifier(1);
    assertEquals(Collections.emptyMap(), res1);

    // Add nodes
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.createPartitions(p1);

    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt");
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt");
    ClassifierInstanceUtils.addChild(p1, "files", f1);
    ClassifierInstanceUtils.addChild(p1, "files", f2);
    client.store(Collections.singletonList(p1));

    // Get data after insertion
    Map<ClassifierKey, ClassifierResult> res2a = client.nodesByClassifier(1);
    // We do not know which ID will be returned but we know how many should be
    // returned and the size
    ClassifierKey keyPartition =
        new ClassifierKey(
            PropertiesLanguage.propertiesLanguage.getKey(),
            PropertiesLanguage.propertiesPartition.getKey());
    ClassifierKey keyFile =
        new ClassifierKey(
            PropertiesLanguage.propertiesLanguage.getKey(),
            PropertiesLanguage.propertiesFile.getKey());
    assertEquals(new HashSet<>(Arrays.asList(keyPartition, keyFile)), res2a.keySet());
    assertEquals(Collections.singleton("p1"), res2a.get(keyPartition).getIds());
    assertEquals(1, res2a.get(keyPartition).getSize());
    assertEquals(1, res2a.get(keyFile).getIds().size());
    assertEquals(2, res2a.get(keyFile).getSize());

    Map<ClassifierKey, ClassifierResult> res2b = client.nodesByClassifier(2);
    Map<ClassifierKey, ClassifierResult> exp2b = new HashMap<>();
    exp2b.put(
        new ClassifierKey(
            PropertiesLanguage.propertiesLanguage.getKey(),
            PropertiesLanguage.propertiesPartition.getKey()),
        new ClassifierResult(new HashSet<>(Collections.singletonList("p1")), 1));
    exp2b.put(
        new ClassifierKey(
            PropertiesLanguage.propertiesLanguage.getKey(),
            PropertiesLanguage.propertiesFile.getKey()),
        new ClassifierResult(new HashSet<>(Arrays.asList("f1", "f2")), 2));
    assertEquals(exp2b, res2b);
  }

  @Test
  public void nodesByLanguage() throws IOException {
    String repoName = "repo_nodesByLanguage";
    NodesLevelInMemoryServerClient client =
        new NodesLevelInMemoryServerClient(getServer(), repoName);
    client.createRepository(
        new RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED));

    // Get data with empty repository
    Map<String, ClassifierResult> res1 = client.nodesByLanguage();
    assertEquals(Collections.emptyMap(), res1);

    // Add nodes
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.createPartitions(p1);

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

  @Test
  public void nodesByLanguageWithLimit() throws IOException {
    String repoName = "repo_nodesByLanguageWithLimit";
    NodesLevelInMemoryServerClient client =
        new NodesLevelInMemoryServerClient(getServer(), repoName);
    client.createRepository(
        new RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED));

    // Get data with empty repository
    Map<String, ClassifierResult> res1 = client.nodesByLanguage(1);
    assertEquals(Collections.emptyMap(), res1);

    // Add nodes
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.createPartitions(p1);

    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt");
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt");
    ClassifierInstanceUtils.addChild(p1, "files", f1);
    ClassifierInstanceUtils.addChild(p1, "files", f2);
    client.store(Collections.singletonList(p1));

    // Get data after insertion
    Map<String, ClassifierResult> res2a = client.nodesByLanguage(1);
    // We do not know which ID will be returned but we know how many should be
    // returned and the size
    assertEquals(
        new HashSet<>(Collections.singletonList(PropertiesLanguage.propertiesLanguage.getKey())),
        res2a.keySet());
    assertEquals(1, res2a.get(PropertiesLanguage.propertiesLanguage.getKey()).getIds().size());
    assertEquals(3, res2a.get(PropertiesLanguage.propertiesLanguage.getKey()).getSize());

    Map<String, ClassifierResult> res2b = client.nodesByLanguage(2);
    assertEquals(
        new HashSet<>(Collections.singletonList(PropertiesLanguage.propertiesLanguage.getKey())),
        res2a.keySet());
    assertEquals(2, res2b.get(PropertiesLanguage.propertiesLanguage.getKey()).getIds().size());
    assertEquals(3, res2b.get(PropertiesLanguage.propertiesLanguage.getKey()).getSize());

    Map<String, ClassifierResult> res2c = client.nodesByLanguage(3);
    Map<String, ClassifierResult> exp2c = new HashMap<>();
    exp2c.put(
        PropertiesLanguage.propertiesLanguage.getKey(),
        new ClassifierResult(new HashSet<>(Arrays.asList("p1", "f1", "f2")), 3));
    assertEquals(exp2c, res2c);
  }
}
