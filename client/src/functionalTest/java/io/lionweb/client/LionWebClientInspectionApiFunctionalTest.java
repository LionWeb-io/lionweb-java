package io.lionweb.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.ClassifierKey;
import io.lionweb.client.api.ClassifierResult;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.languages.PropertiesLanguage;
import io.lionweb.client.testing.AbstractClientFunctionalTest;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.impl.DynamicNode;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class LionWebClientInspectionApiFunctionalTest extends AbstractClientFunctionalTest {

  public LionWebClientInspectionApiFunctionalTest() {
    super(LionWebVersion.v2023_1, false);
  }

  @Test
  public void nodesByClassifier() throws IOException {
    LionWebClient client =
        new LionWebClient(
            LionWebVersion.v2023_1, "localhost", getServerPort(), "repo_nodesByClassifier");
    client.createRepository(
        new RepositoryConfiguration(
            "repo_nodesByClassifier", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Get data with empty repository
    Map<ClassifierKey, ClassifierResult> res1 = client.nodesByClassifier();
    assertEquals(Collections.emptyMap(), res1);

    // Add nodes
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.rawCreatePartitions(client.getJsonSerialization().serializeNodesToJsonString(p1));

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
    LionWebClient client =
        new LionWebClient(
            LionWebVersion.v2023_1,
            "localhost",
            getServerPort(),
            "repo_nodesByClassifierWithLimit");
    client.createRepository(
        new RepositoryConfiguration(
            "repo_nodesByClassifierWithLimit", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Get data with empty repository
    Map<ClassifierKey, ClassifierResult> res1 = client.nodesByClassifier(1);
    assertEquals(Collections.emptyMap(), res1);

    // Add nodes
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.rawCreatePartitions(client.getJsonSerialization().serializeNodesToJsonString(p1));

    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt");
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt");
    ClassifierInstanceUtils.addChild(p1, "files", f1);
    ClassifierInstanceUtils.addChild(p1, "files", f2);
    client.store(Collections.singletonList(p1));

    // Get data after insertion
    Map<ClassifierKey, ClassifierResult> res2a = client.nodesByClassifier(1);
    Map<ClassifierKey, ClassifierResult> exp2a = new HashMap<>();
    exp2a.put(
        new ClassifierKey(
            PropertiesLanguage.propertiesLanguage.getKey(),
            PropertiesLanguage.propertiesPartition.getKey()),
        new ClassifierResult(new HashSet<>(Arrays.asList("p1")), 1));
    exp2a.put(
        new ClassifierKey(
            PropertiesLanguage.propertiesLanguage.getKey(),
            PropertiesLanguage.propertiesFile.getKey()),
        new ClassifierResult(new HashSet<>(Arrays.asList("f1")), 2));
    assertEquals(exp2a, res2a);

    Map<ClassifierKey, ClassifierResult> res2b = client.nodesByClassifier(2);
    Map<ClassifierKey, ClassifierResult> exp2b = new HashMap<>();
    exp2b.put(
        new ClassifierKey(
            PropertiesLanguage.propertiesLanguage.getKey(),
            PropertiesLanguage.propertiesPartition.getKey()),
        new ClassifierResult(new HashSet<>(Arrays.asList("p1")), 1));
    exp2b.put(
        new ClassifierKey(
            PropertiesLanguage.propertiesLanguage.getKey(),
            PropertiesLanguage.propertiesFile.getKey()),
        new ClassifierResult(new HashSet<>(Arrays.asList("f1", "f2")), 2));
    assertEquals(exp2b, res2b);
  }

  @Test
  public void nodesByLanguage() throws IOException {
    LionWebClient client =
        new LionWebClient(
            LionWebVersion.v2023_1, "localhost", getServerPort(), "repo_nodesByLanguage");
    client.createRepository(
        new RepositoryConfiguration(
            "repo_nodesByLanguage", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Get data with empty repository
    Map<String, ClassifierResult> res1 = client.nodesByLanguage();
    assertEquals(Collections.emptyMap(), res1);

    // Add nodes
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.rawCreatePartitions(client.getJsonSerialization().serializeNodesToJsonString(p1));

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
    LionWebClient client =
        new LionWebClient(
            LionWebVersion.v2023_1, "localhost", getServerPort(), "repo_nodesByLanguageWithLimit");
    client.createRepository(
        new RepositoryConfiguration(
            "repo_nodesByLanguageWithLimit", LionWebVersion.v2023_1, HistorySupport.DISABLED));
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Get data with empty repository
    Map<String, ClassifierResult> res1 = client.nodesByLanguage(1);
    assertEquals(Collections.emptyMap(), res1);

    // Add nodes
    DynamicNode p1 = new DynamicNode("p1", PropertiesLanguage.propertiesPartition);
    client.rawCreatePartitions(client.getJsonSerialization().serializeNodesToJsonString(p1));

    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt");
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesFile);
    ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt");
    ClassifierInstanceUtils.addChild(p1, "files", f1);
    ClassifierInstanceUtils.addChild(p1, "files", f2);
    client.store(Collections.singletonList(p1));

    // Get data after insertion
    Map<String, ClassifierResult> res2a = client.nodesByLanguage(1);
    Map<String, ClassifierResult> exp2a = new HashMap<>();
    exp2a.put(
        PropertiesLanguage.propertiesLanguage.getKey(),
        new ClassifierResult(new HashSet<>(Arrays.asList("p1")), 3));
    assertEquals(exp2a, res2a);

    Map<String, ClassifierResult> res2b = client.nodesByLanguage(2);
    Map<String, ClassifierResult> exp2b = new HashMap<>();
    exp2b.put(
        PropertiesLanguage.propertiesLanguage.getKey(),
        new ClassifierResult(new HashSet<>(Arrays.asList("p1", "f1")), 3));
    assertEquals(exp2b, res2b);

    Map<String, ClassifierResult> res2c = client.nodesByLanguage(3);
    Map<String, ClassifierResult> exp2c = new HashMap<>();
    exp2c.put(
        PropertiesLanguage.propertiesLanguage.getKey(),
        new ClassifierResult(new HashSet<>(Arrays.asList("p1", "f1", "f2")), 3));
    assertEquals(exp2c, res2c);
  }
}
