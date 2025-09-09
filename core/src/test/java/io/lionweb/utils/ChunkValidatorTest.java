package io.lionweb.utils;

import static org.junit.Assert.*;

import io.lionweb.serialization.data.*;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ChunkValidatorTest {

  private ChunkValidator validator;
  private SerializedChunk chunk;
  private UsedLanguage testLanguage;
  private MetaPointer testMetaPointer;

  @Before
  public void setUp() {
    validator = new ChunkValidator();
    chunk = new SerializedChunk();
    testLanguage = new UsedLanguage("test-lang", "1.0");
    testMetaPointer = MetaPointer.get("test-lang", "1.0", "TestProperty");
  }

  @Test
  public void testValidChunk() {
    // Create valid nodes
    SerializedClassifierInstance parent = createValidNode("parent1", null);
    SerializedClassifierInstance child = createValidNode("child1", "parent1");

    // Set up containment relationship
    SerializedContainmentValue containment =
        new SerializedContainmentValue(testMetaPointer, Arrays.asList("child1"));
    parent.addContainmentValue(containment);

    chunk.addClassifierInstances(parent, child);
    chunk.addLanguage(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertTrue(result.isSuccessful());
  }

  @Test
  public void testInvalidNodeId() {
    SerializedClassifierInstance node = createValidNode("", null);
    chunk.addClassifierInstances(node);
    chunk.addLanguage(testLanguage);

    ValidationResult result = validator.validate(chunk);

    Assert.assertFalse(result.isSuccessful());
    Assert.assertEquals(1, result.getIssues().size());
    Assert.assertTrue(
        result.getIssues().iterator().next().getMessage().contains("Invalid node id"));
  }

  @Test
  public void testDuplicateNodeIds() {
    SerializedClassifierInstance node1 = createValidNode("duplicate", null);
    SerializedClassifierInstance node2 = createValidNode("duplicate", null);

    chunk.addClassifierInstances(node1, node2);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("Duplicate node id: duplicate")));
  }

  @Test
  public void testLanguageMismatch() {
    SerializedClassifierInstance node = createValidNode("node1", null);

    // Add a different language to chunk than what's used by the node
    UsedLanguage differentLanguage = new UsedLanguage("different-lang", "1.0");

    chunk.addClassifierInstances(node);
    chunk.addLanguages(differentLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("We expected these used languages")));
  }

  @Test
  public void testNodeInMultipleContainments() {
    SerializedClassifierInstance parent1 = createValidNode("parent1", null);
    SerializedClassifierInstance parent2 = createValidNode("parent2", null);
    SerializedClassifierInstance child = createValidNode("child1", "parent1");

    // Add child to both parents' containments
    SerializedContainmentValue containment1 = createContainment("child1");
    SerializedContainmentValue containment2 = createContainment("child1");

    parent1.addContainmentValue(containment1);
    parent2.addContainmentValue(containment2);

    chunk.addClassifierInstances(parent1, parent2, child);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("child1 is listed in multiple places")));
  }

  @Test
  public void testNodeInMultipleAnnotations() {
    SerializedClassifierInstance parent1 = createValidNode("parent1", null);
    SerializedClassifierInstance parent2 = createValidNode("parent2", null);
    SerializedClassifierInstance annotation = createValidNode("annotation1", "parent1");

    // Add annotation to both parents
    parent1.addAnnotation("annotation1");
    parent2.addAnnotation("annotation1");

    chunk.addClassifierInstances(parent1, parent2, annotation);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error -> error.getMessage().contains("annotation1 is listed in multiple places")));
  }

  @Test
  public void testContainmentParentMismatch() {
    SerializedClassifierInstance parent = createValidNode("parent1", null);
    SerializedClassifierInstance child = createValidNode("child1", "differentParent");

    // Parent contains child, but child's parent ID is different
    SerializedContainmentValue containment = createContainment("child1");
    parent.addContainmentValue(containment);

    chunk.addClassifierInstances(parent, child);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error ->
                    error
                        .getMessage()
                        .contains(
                            "child1 is listed as child of parent1 but it has as parent differentParent")));
  }

  @Test
  public void testAnnotationParentMismatch() {
    SerializedClassifierInstance parent = createValidNode("parent1", null);
    SerializedClassifierInstance annotation = createValidNode("annotation1", "differentParent");

    // Parent has annotation, but annotation's parent ID is different
    parent.addAnnotation("annotation1");

    chunk.addClassifierInstances(parent, annotation);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error ->
                    error
                        .getMessage()
                        .contains(
                            "annotation1 is listed as an annotation of parent1 but it has as parent differentParent")));
  }

  @Test
  public void testParentNotContainingChild() {
    SerializedClassifierInstance parent = createValidNode("parent1", null);
    SerializedClassifierInstance child = createValidNode("child1", "parent1");

    // Child has parent, but parent doesn't contain child
    chunk.addClassifierInstances(parent, child);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error ->
                    error
                        .getMessage()
                        .contains(
                            "child1 list as parent parent1 but such parent does not contain it")));
  }

  @Test
  public void testEmptyChunk() {
    ValidationResult result = validator.validate(chunk);

    assertTrue(result.isSuccessful());
    assertTrue(result.getIssues().isEmpty());
  }

  @Test
  public void testNodeWithPropertiesAndReferences() {
    UsedLanguage propLanguage = new UsedLanguage("prop-lang", "1.0");
    UsedLanguage refLanguage = new UsedLanguage("ref-lang", "1.0");

    SerializedClassifierInstance node = createValidNode("node1", null);

    // Add property with different language
    SerializedPropertyValue property =
        SerializedPropertyValue.get(MetaPointer.get("prop-lang", "1.0", "TestProp"), "test value");
    node.addPropertyValue(property);

    // Add reference with different language
    SerializedReferenceValue reference =
        new SerializedReferenceValue(MetaPointer.get("ref-lang", "1.0", "TestRef"));
    reference.addValue(new SerializedReferenceValue.Entry("target1", "resolveInfo"));
    node.addReferenceValue(reference);

    chunk.addClassifierInstances(node);
    chunk.addLanguages(testLanguage, propLanguage, refLanguage);

    ValidationResult result = validator.validate(chunk);

    assertTrue(result.isSuccessful());
  }

  @Test
  public void testMultipleValidationErrors() {
    // Create nodes with multiple issues
    SerializedClassifierInstance invalidNode1 = createValidNode("", null); // Invalid ID
    SerializedClassifierInstance duplicateNode1 = createValidNode("dup", null);
    SerializedClassifierInstance duplicateNode2 = createValidNode("dup", null); // Duplicate ID

    chunk.addClassifierInstances(invalidNode1, duplicateNode1, duplicateNode2);
    chunk.addLanguages(new UsedLanguage("wrong-lang", "1.0")); // Wrong language

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(result.getIssues().size() >= 2); // Should have multiple errors
  }

  // Helper methods

  private SerializedClassifierInstance createValidNode(String id, String parentId) {
    SerializedClassifierInstance node = new SerializedClassifierInstance();
    node.setID(id);
    node.setParentNodeID(parentId);

    node.setClassifier(testMetaPointer);

    return node;
  }

  private SerializedContainmentValue createContainment(String childId) {
    SerializedContainmentValue containment =
        new SerializedContainmentValue(testMetaPointer, childId);
    return containment;
  }
}
