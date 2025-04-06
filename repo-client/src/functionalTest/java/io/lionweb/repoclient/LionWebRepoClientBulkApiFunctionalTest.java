package io.lionweb.repoclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import io.lionweb.repoclient.testing.AbstractRepoClientFunctionalTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.assertEquals;

@Testcontainers
public class LionWebRepoClientBulkApiFunctionalTest extends AbstractRepoClientFunctionalTest {

  @Test
  public void noPartitionsOnNewModelRepository() throws IOException {
    LionWebRepoClient client = new LionWebRepoClient(LionWebVersion.v2023_1, "localhost", getModelRepoPort(), "default");
    List<Node> partitions = client.listPartitions();
    assertEquals(Collections.emptyList(), partitions);
  }

  @Test
  public void partitionsCRUD() throws IOException {
    LionWebRepoClient client = new LionWebRepoClient(LionWebVersion.v2023_1, "localhost", getModelRepoPort(), "default");
    client.getJsonSerialization().registerLanguage(PropertiesLanguage.propertiesLanguage);

    // Create partition
    DynamicNode f1 = new DynamicNode("f1", PropertiesLanguage.propertiesFile);
    client.createPartitions(client.getJsonSerialization().serializeNodesToJsonString(f1));

    // Check list
    List<Node> nodes1 = client.listPartitions();
    assertEquals(1, nodes1.size());
    assertEquals("f1", nodes1.get(0).getID());
    assertEquals(PropertiesLanguage.propertiesFile, nodes1.get(0).getClassifier());
    assertEquals(Arrays.asList("f1"), client.listPartitionsIDs());

    // Create partitions
    DynamicNode f2 = new DynamicNode("f2", PropertiesLanguage.propertiesFile);
    DynamicNode f3 = new DynamicNode("f3", PropertiesLanguage.propertiesFile);
    client.createPartitions(client.getJsonSerialization().serializeNodesToJsonString(f2, f3));

    // Check list
    List<Node> nodes2 = client.listPartitions();
    assertEquals(3, nodes2.size());
    assertEquals(new HashSet<>(Arrays.asList("f1", "f2", "f3")), new HashSet<>(client.listPartitionsIDs()));

    // Delete partitions
    client.deletePartitions(Arrays.asList("f1", "f3"));

    // Check list
    List<Node> nodes3 = client.listPartitions();
    assertEquals(1, nodes3.size());
    assertEquals("f2", nodes3.get(0).getID());
    assertEquals(PropertiesLanguage.propertiesFile, nodes3.get(0).getClassifier());
    assertEquals(Arrays.asList("f2"), client.listPartitionsIDs());

    // Delete partition
    client.deletePartitions(Arrays.asList("f2"));

    // Check list
    List<Node> nodes4 = client.listPartitions();
    assertEquals(0, nodes4.size());
    assertEquals(Collections.emptyList(), client.listPartitionsIDs());
  }


  // TODO ids
  // TODO store
  // TODO retrieve

}
