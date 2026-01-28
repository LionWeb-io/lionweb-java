package io.lionweb.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

public class SerializedJsonComparisonUtilsTest {

  // ========== assertEquivalentLionWebJson() Tests ==========

  @Test
  public void testAssertEquivalentLionWebJson_identical() {
    JsonObject expected = createMinimalLionWebJson();
    JsonObject actual = createMinimalLionWebJson();

    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
    // No exception means success
  }

  @Test
  public void testAssertEquivalentLionWebJson_withNodes() {
    JsonObject expected = createLionWebJsonWithNodes();
    JsonObject actual = createLionWebJsonWithNodes();

    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentLionWebJson_withLanguages() {
    JsonObject expected = createLionWebJsonWithLanguages();
    JsonObject actual = createLionWebJsonWithLanguages();

    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentLionWebJson_differentSerializationVersion() {
    JsonObject expected = createMinimalLionWebJson();
    JsonObject actual = createMinimalLionWebJson();
    actual.addProperty("serializationFormatVersion", "2024.1");

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      assertTrue(e.getMessage().contains("serializationFormatVersion"));
    }
  }

  @Test
  public void testAssertEquivalentLionWebJson_expectedMissingKey() {
    JsonObject expected = new JsonObject();
    expected.addProperty("serializationFormatVersion", "2023.1");
    expected.add("languages", new JsonArray());
    // Missing "nodes"

    JsonObject actual = createMinimalLionWebJson();

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("irregular keys"));
      assertTrue(e.getMessage().contains("expected"));
    }
  }

  @Test
  public void testAssertEquivalentLionWebJson_actualMissingKey() {
    JsonObject expected = createMinimalLionWebJson();

    JsonObject actual = new JsonObject();
    actual.addProperty("serializationFormatVersion", "2023.1");
    actual.add("languages", new JsonArray());
    // Missing "nodes"

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("irregular keys"));
      assertTrue(e.getMessage().contains("actual"));
    }
  }

  @Test
  public void testAssertEquivalentLionWebJson_extraKey() {
    JsonObject expected = createMinimalLionWebJson();

    JsonObject actual = createMinimalLionWebJson();
    actual.addProperty("extraKey", "extraValue");

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("irregular keys"));
    }
  }

  // ========== Languages Comparison Tests ==========

  @Test
  public void testAssertEquivalentLionWebJsonLanguages_differentSize() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedLangs = new JsonArray();
    expectedLangs.add(createLanguage("lang1", "1.0"));
    expected.add("languages", expectedLangs);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualLangs = new JsonArray();
    actualLangs.add(createLanguage("lang1", "1.0"));
    actualLangs.add(createLanguage("lang2", "2.0"));
    actual.add("languages", actualLangs);

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      assertTrue(e.getMessage().contains("Expected 1 languages"));
      assertTrue(e.getMessage().contains("but found 2"));
    }
  }

  @Test
  public void testAssertEquivalentLionWebJsonLanguages_differentVersions() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedLangs = new JsonArray();
    expectedLangs.add(createLanguage("lang1", "1.0"));
    expected.add("languages", expectedLangs);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualLangs = new JsonArray();
    actualLangs.add(createLanguage("lang1", "2.0"));
    actual.add("languages", actualLangs);

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      assertTrue(e.getMessage().contains("Used languages do not match"));
    }
  }

  @Test
  public void testAssertEquivalentLionWebJsonLanguages_differentKeys() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedLangs = new JsonArray();
    expectedLangs.add(createLanguage("lang1", "1.0"));
    expected.add("languages", expectedLangs);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualLangs = new JsonArray();
    actualLangs.add(createLanguage("lang2", "1.0"));
    actual.add("languages", actualLangs);

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      assertTrue(e.getMessage().contains("Used languages do not match"));
    }
  }

  @Test
  public void testAssertEquivalentLionWebJsonLanguages_multipleLanguagesDifferentOrder() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedLangs = new JsonArray();
    expectedLangs.add(createLanguage("lang1", "1.0"));
    expectedLangs.add(createLanguage("lang2", "2.0"));
    expected.add("languages", expectedLangs);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualLangs = new JsonArray();
    actualLangs.add(createLanguage("lang2", "2.0"));
    actualLangs.add(createLanguage("lang1", "1.0"));
    actual.add("languages", actualLangs);

    // Should pass - order doesn't matter
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  // ========== Nodes Comparison Tests ==========

  @Test
  public void testAssertEquivalentLionWebJsonNodes_unexpectedID() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    expectedNodes.add(createNode("node1"));
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    actualNodes.add(createNode("node1"));
    actualNodes.add(createNode("node2"));
    actual.add("nodes", actualNodes);

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      assertTrue(e.getMessage().contains("Unexpected IDs found"));
      assertTrue(e.getMessage().contains("node2"));
    }
  }

  @Test
  public void testAssertEquivalentLionWebJsonNodes_missingID() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    expectedNodes.add(createNode("node1"));
    expectedNodes.add(createNode("node2"));
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    actualNodes.add(createNode("node1"));
    actual.add("nodes", actualNodes);

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      assertTrue(e.getMessage().contains("Missing IDs found"));
      assertTrue(e.getMessage().contains("node2"));
    }
  }

  @Test
  public void testAssertEquivalentLionWebJsonNodes_differentOrder() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    expectedNodes.add(createNode("node1"));
    expectedNodes.add(createNode("node2"));
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    actualNodes.add(createNode("node2"));
    actualNodes.add(createNode("node1"));
    actual.add("nodes", actualNodes);

    // Should pass - order doesn't matter for nodes at top level
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  // ========== Node Structure Tests ==========

  @Test
  public void testAssertEquivalentNodes_withParentNull() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    node1.add("parent", JsonNull.INSTANCE);
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    node2.add("parent", JsonNull.INSTANCE);
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentNodes_parentNullIgnored() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    node2.add("parent", JsonNull.INSTANCE);
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    // Should pass - null parent is ignored
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentNodes_withClassifier() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    node1.addProperty("classifier", "Concept1");
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    node2.addProperty("classifier", "Concept1");
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentNodes_differentClassifier() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    node1.addProperty("classifier", "Concept1");
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    node2.addProperty("classifier", "Concept2");
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      assertTrue(e.getMessage().contains("different classifier"));
    }
  }

  @Test
  public void testAssertEquivalentNodes_withProperties() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    JsonArray props = new JsonArray();
    JsonObject prop = new JsonObject();
    prop.addProperty("property", "name");
    prop.addProperty("value", "John");
    props.add(prop);
    node1.add("properties", props);
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    node2.add("properties", props);
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentNodes_propertiesUnorderedMatch() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    JsonArray props1 = new JsonArray();
    props1.add(createProperty("name", "John"));
    props1.add(createProperty("age", "30"));
    node1.add("properties", props1);
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    JsonArray props2 = new JsonArray();
    props2.add(createProperty("age", "30"));
    props2.add(createProperty("name", "John"));
    node2.add("properties", props2);
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    // Should pass - properties order doesn't matter
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentNodes_withReferences() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    JsonArray refs = new JsonArray();
    JsonObject ref = new JsonObject();
    ref.addProperty("reference", "ref1");
    JsonArray targets = new JsonArray();
    targets.add(new JsonPrimitive("target1"));
    ref.add("targets", targets);
    refs.add(ref);
    node1.add("references", refs);
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    node2.add("references", refs);
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentNodes_referencesEmptyFiltered() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    JsonArray refs1 = new JsonArray();
    JsonObject ref1 = new JsonObject();
    ref1.addProperty("reference", "ref1");
    ref1.add("targets", new JsonArray()); // Empty targets
    refs1.add(ref1);
    node1.add("references", refs1);
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    node2.add("references", new JsonArray()); // No references at all
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    // Should pass - empty references are filtered
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentNodes_withContainments() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    JsonArray containments = new JsonArray();
    JsonObject containment = new JsonObject();
    containment.addProperty("containment", "children");
    JsonArray children = new JsonArray();
    children.add(new JsonPrimitive("child1"));
    containment.add("children", children);
    containments.add(containment);
    node1.add("containments", containments);
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    node2.add("containments", containments);
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentNodes_containmentsEmptyFiltered() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    JsonArray containments1 = new JsonArray();
    JsonObject containment1 = new JsonObject();
    containment1.addProperty("containment", "children");
    containment1.add("children", new JsonArray()); // Empty children
    containments1.add(containment1);
    node1.add("containments", containments1);
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    node2.add("containments", new JsonArray()); // No containments at all
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    // Should pass - empty containments are filtered
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  @Test
  public void testAssertEquivalentNodes_missingKey() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    node1.addProperty("classifier", "Concept1");
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      assertTrue(e.getMessage().contains("Missing keys found"));
      assertTrue(e.getMessage().contains("classifier"));
    }
  }

  // ========== Unordered Arrays Tests ==========

  @Test
  public void testAssertEquivalentUnorderedArrays_differentSizes() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    JsonArray props1 = new JsonArray();
    props1.add(createProperty("name", "John"));
    node1.add("properties", props1);
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    JsonArray props2 = new JsonArray();
    props2.add(createProperty("name", "John"));
    props2.add(createProperty("age", "30"));
    node2.add("properties", props2);
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      assertTrue(e.getMessage().contains("Arrays with different sizes"));
    }
  }

  @Test
  public void testAssertEquivalentUnorderedArrays_noEquivalent() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    JsonArray props1 = new JsonArray();
    props1.add(createProperty("name", "John"));
    node1.add("properties", props1);
    expectedNodes.add(node1);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualNodes = new JsonArray();
    JsonObject node2 = createNode("node1");
    JsonArray props2 = new JsonArray();
    props2.add(createProperty("name", "Jane"));
    node2.add("properties", props2);
    actualNodes.add(node2);
    actual.add("nodes", actualNodes);

    try {
      SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
      fail("Expected AssertionError");
    } catch (AssertionError e) {
      assertTrue(e.getMessage().contains("no equivalent"));
    }
  }

  // ========== Complex Integration Tests ==========

  @Test
  public void testComplexLionWebJsonComparison() {
    JsonObject expected = createMinimalLionWebJson();
    JsonArray expectedLangs = new JsonArray();
    expectedLangs.add(createLanguage("lang1", "1.0"));
    expectedLangs.add(createLanguage("lang2", "2.0"));
    expected.add("languages", expectedLangs);

    JsonArray expectedNodes = new JsonArray();
    JsonObject node1 = createNode("node1");
    node1.addProperty("classifier", "Concept1");
    JsonArray props = new JsonArray();
    props.add(createProperty("name", "Test"));
    props.add(createProperty("value", "123"));
    node1.add("properties", props);
    expectedNodes.add(node1);

    JsonObject node2 = createNode("node2");
    node2.addProperty("classifier", "Concept2");
    node2.addProperty("parent", "node1");
    expectedNodes.add(node2);
    expected.add("nodes", expectedNodes);

    JsonObject actual = createMinimalLionWebJson();
    JsonArray actualLangs = new JsonArray();
    actualLangs.add(createLanguage("lang2", "2.0"));
    actualLangs.add(createLanguage("lang1", "1.0"));
    actual.add("languages", actualLangs);

    JsonArray actualNodes = new JsonArray();
    JsonObject actualNode2 = createNode("node2");
    actualNode2.addProperty("classifier", "Concept2");
    actualNode2.addProperty("parent", "node1");
    actualNodes.add(actualNode2);

    JsonObject actualNode1 = createNode("node1");
    actualNode1.addProperty("classifier", "Concept1");
    JsonArray actualProps = new JsonArray();
    actualProps.add(createProperty("value", "123"));
    actualProps.add(createProperty("name", "Test"));
    actualNode1.add("properties", actualProps);
    actualNodes.add(actualNode1);
    actual.add("nodes", actualNodes);

    // Should pass - order differences should not matter
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, actual);
  }

  // ========== Helper Methods ==========

  private JsonObject createMinimalLionWebJson() {
    JsonObject json = new JsonObject();
    json.addProperty("serializationFormatVersion", "2023.1");
    json.add("languages", new JsonArray());
    json.add("nodes", new JsonArray());
    return json;
  }

  private JsonObject createLionWebJsonWithNodes() {
    JsonObject json = createMinimalLionWebJson();
    JsonArray nodes = new JsonArray();
    nodes.add(createNode("node1"));
    json.add("nodes", nodes);
    return json;
  }

  private JsonObject createLionWebJsonWithLanguages() {
    JsonObject json = createMinimalLionWebJson();
    JsonArray languages = new JsonArray();
    languages.add(createLanguage("lang1", "1.0"));
    json.add("languages", languages);
    return json;
  }

  private JsonObject createNode(String id) {
    JsonObject node = new JsonObject();
    node.addProperty("id", id);
    return node;
  }

  private JsonObject createLanguage(String key, String version) {
    JsonObject language = new JsonObject();
    language.addProperty("key", key);
    language.addProperty("version", version);
    return language;
  }

  private JsonObject createProperty(String property, String value) {
    JsonObject prop = new JsonObject();
    prop.addProperty("property", property);
    prop.addProperty("value", value);
    return prop;
  }
}
