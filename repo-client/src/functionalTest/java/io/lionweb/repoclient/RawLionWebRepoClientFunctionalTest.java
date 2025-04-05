package io.lionweb.repoclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.repoclient.testing.AbstractRepoClientFunctionalTest;

import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.assertEquals;

@Testcontainers
public class RawLionWebRepoClientFunctionalTest extends AbstractRepoClientFunctionalTest {

  @Test
  public void noPartitionsOnNewModelRepository() throws IOException {
    RawLionWebRepoClient client = new RawLionWebRepoClient("localhost", getModelRepoPort());
    // {"chunk":{"serializationFormatVersion":"2023.1","languages":[],"nodes":[]},"success":true,
    // "messages":[{"kind":"RepoVersion","message":"RepositoryVersion at end of Transaction","data":{"version":"0"}}]}
    JsonObject response = JsonParser.parseString(client.listPartitions()).getAsJsonObject();
    assertEquals(true, response.get("success").getAsBoolean());
    JsonObject chunk = response.get("chunk").getAsJsonObject();
    assertEquals("2023.1", chunk.get("serializationFormatVersion").getAsString());
    assertEquals(new JsonArray(), chunk.get("languages").getAsJsonArray());
    assertEquals(new JsonArray(), chunk.get("nodes").getAsJsonArray());
  }
//
//  @Test
//  public void isNodeExisting() {
//    AdvancedLionWebRepoClient client = new AdvancedLionWebRepoClient("localhost", modelRepoPort);
//
//    assertFalse(client.isNodeExisting("pp1"));
//    assertFalse(client.isNodeExisting("pf1"));
//    assertFalse(client.isNodeExisting("prop1"));
//    assertFalse(client.isNodeExisting("prop2"));
//    assertFalse(client.isNodeExisting("prop3"));
//
//    Node pp1 =
//        DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.propertiesPartition, "pp1");
//    client.createPartition(pp1);
//
//    assertTrue(client.isNodeExisting("pp1"));
//    assertFalse(client.isNodeExisting("pf1"));
//
//    Node pf = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.propertiesFile, "pf1");
//    pf.setParent(pp1);
//
//    Node prop1 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property, "prop1");
//    setPropertyValueByName(prop1, "name", "Prop1");
//    pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop1);
//
//    Node prop2 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property, "prop2");
//    setPropertyValueByName(prop2, "name", "Prop2");
//    pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop2);
//
//    Node prop3 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property, "prop3");
//    setPropertyValueByName(prop3, "name", "Prop3");
//    pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop3);
//
//    client.storeTree(pf);
//
//    assertTrue(client.isNodeExisting("pp1"));
//    assertTrue(client.isNodeExisting("pf1"));
//    assertTrue(client.isNodeExisting("prop1"));
//    assertTrue(client.isNodeExisting("prop2"));
//    assertTrue(client.isNodeExisting("prop3"));
//
//    client.deletePartition("pp1");
//    assertFalse(client.isNodeExisting("pp1"));
//    assertFalse(client.isNodeExisting("pf1"));
//    assertFalse(client.isNodeExisting("prop1"));
//    assertFalse(client.isNodeExisting("prop2"));
//    assertFalse(client.isNodeExisting("prop3"));
//  }
//
//  @Test
//  public void gettingPartitionsAfterStoringPartitions() {
//    LionWebClient client = new LionWebClient("localhost", modelRepoPort);
//    client.registerLanguage(PropertiesLanguageDefinition.propertiesLanguage);
//
//    Node pp1 =
//        DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.propertiesPartition, "pp1");
//    assertEquals(Collections.emptyList(), client.getPartitionIDs());
//    client.createPartition(pp1);
//    assertEquals(Collections.singletonList("pp1"), client.getPartitionIDs());
//    assertEquals(pp1.getId(), client.retrieve("pp1").getId());
//  }
//
//  @Test
//  public void gettingNodesAfterStoringNodes() {
//    LionWebClient client = new LionWebClient("localhost", modelRepoPort);
//    client.registerLanguage(PropertiesLanguageDefinition.propertiesLanguage);
//
//    Node pp1 =
//        DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.propertiesPartition, "pp1");
//    client.createPartition(pp1);
//
//    Node pf = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.propertiesFile, "pf1");
//    pf.setParent(pp1);
//
//    Node prop1 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property, "prop1");
//    setPropertyValueByName(prop1, "name", "Prop1");
//    pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop1);
//
//    Node prop2 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property);
//    setPropertyValueByName(prop2, "name", "Prop2");
//    pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop2);
//
//    Node prop3 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property);
//    setPropertyValueByName(prop3, "name", "Prop3");
//    pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop3);
//
//    client.storeTree(pf);
//
//    Node retrieved = client.retrieve("pf1");
//    assertNull(retrieved.getParent());
//    assertEquals("pf1", retrieved.getId());
//    assertEquals(PropertiesLanguageDefinition.propertiesFile, retrieved.getClassifier());
//  }
//
//  // Remaining tests follow the same structure, can be translated if you'd like me to continue
}
