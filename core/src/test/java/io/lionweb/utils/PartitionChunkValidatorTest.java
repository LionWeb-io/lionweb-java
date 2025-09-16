package io.lionweb.utils;

import static org.junit.Assert.*;

import io.lionweb.serialization.data.*;
import org.junit.Before;
import org.junit.Test;

public class PartitionChunkValidatorTest {

  private PartitionChunkValidator validator;
  private SerializationChunk chunk;
  private LanguageVersion testLanguage;
  private MetaPointer testMetaPointer;

  @Before
  public void setUp() {
    validator = new PartitionChunkValidator();
    chunk = new SerializationChunk();
    testLanguage = LanguageVersion.of("test-lang", "1.0");
    testMetaPointer = MetaPointer.get("test-lang", "1.0", "TestContainment");
  }

  @Test
  public void testValidPartitionChunk() {
    // Create valid partition: root -> child1, child2
    SerializedClassifierInstance root = createValidNode("root1", null);
    SerializedClassifierInstance child1 = createValidNode("child1", "root1");
    SerializedClassifierInstance child2 = createValidNode("child2", "root1");

    // Set up containment relationship
    SerializedContainmentValue containment =
        new SerializedContainmentValue(testMetaPointer, "child1", "child2");
    root.addContainmentValue(containment);

    chunk.addClassifierInstances(root, child1, child2);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertTrue(result.isSuccessful());
    assertTrue(result.getIssues().isEmpty());
  }

  @Test
  public void testMissingContainedNode() {
    SerializedClassifierInstance root = createValidNode("root1", null);
    SerializedClassifierInstance child1 = createValidNode("child1", "root1");

    // Root contains child1 and child2, but child2 is not in the chunk
    SerializedContainmentValue containment =
        new SerializedContainmentValue(
            testMetaPointer, "child1", "child2"); // child2 is missing from chunk
    root.addContainmentValue(containment);

    chunk.addClassifierInstances(root, child1); // child2 is missing
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("Missing node: child2")));
  }

  @Test
  public void testMissingAnnotationNode() {
    SerializedClassifierInstance root = createValidNode("root1", null);

    // Root has annotation1, but annotation1 is not in the chunk
    root.addAnnotation("annotation1");

    chunk.addClassifierInstances(root); // annotation1 is missing
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("Missing node: annotation1")));
  }

  @Test
  public void testMultipleMissingNodes() {
    SerializedClassifierInstance root = createValidNode("root1", null);

    // Root contains missing children and has missing annotations
    SerializedContainmentValue containment =
        new SerializedContainmentValue(testMetaPointer, "missingChild1", "missingChild2");
    root.addContainmentValue(containment);

    root.addAnnotation("missingAnnotation1");
    root.addAnnotation("missingAnnotation2");

    chunk.addClassifierInstances(root);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertEquals(5, result.getIssues().size());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("Missing node: missingChild1")));
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("Missing node: missingChild2")));
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("Missing node: missingAnnotation1")));
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("Missing node: missingAnnotation2")));
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error ->
                    error
                        .getMessage()
                        .contains(
                            "Some nodes should be contained, but are not present: missingAnnotation1, missingAnnotation2, missingChild1, missingChild2")));
  }

  @Test
  public void testNoRootNodes() {
    // Create nodes that all have parents
    SerializedClassifierInstance child1 = createValidNode("child1", "parent1");
    SerializedClassifierInstance child2 = createValidNode("child2", "parent1");

    chunk.addClassifierInstances(child1, child2);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error -> error.getMessage().contains("Expected exactly one root, found: []")));
  }

  @Test
  public void testMultipleRootNodes() {
    // Create multiple nodes without parents
    SerializedClassifierInstance root1 = createValidNode("root1", null);
    SerializedClassifierInstance root2 = createValidNode("root2", null);
    SerializedClassifierInstance root3 = createValidNode("root3", null);

    chunk.addClassifierInstances(root1, root2, root3);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error ->
                    error
                            .getMessage()
                            .contains("Expected exactly one root, found: [root1, root2, root3]")
                        || error
                            .getMessage()
                            .contains("Expected exactly one root, found: [root1, root3, root2]")
                        || error
                            .getMessage()
                            .contains("Expected exactly one root, found: [root2, root1, root3]")
                        || error
                            .getMessage()
                            .contains("Expected exactly one root, found: [root2, root3, root1]")
                        || error
                            .getMessage()
                            .contains("Expected exactly one root, found: [root3, root1, root2]")
                        || error
                            .getMessage()
                            .contains("Expected exactly one root, found: [root3, root2, root1]")));
  }

  @Test
  public void testEmptyChunk() {
    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error -> error.getMessage().contains("Expected exactly one root, found: []")));
  }

  @Test
  public void testDeepHierarchy() {
    // Create a deep hierarchy: root -> child1 -> grandchild1
    SerializedClassifierInstance root = createValidNode("root", null);
    SerializedClassifierInstance child1 = createValidNode("child1", "root");
    SerializedClassifierInstance grandchild1 = createValidNode("grandchild1", "child1");

    // Set up containment relationships
    SerializedContainmentValue rootContainment =
        new SerializedContainmentValue(testMetaPointer, "child1");
    root.addContainmentValue(rootContainment);

    SerializedContainmentValue childContainment =
        new SerializedContainmentValue(testMetaPointer, "grandchild1");
    child1.addContainmentValue(childContainment);

    chunk.addClassifierInstances(root, child1, grandchild1);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertTrue(result.isSuccessful());
    assertTrue(result.getIssues().isEmpty());
  }

  @Test
  public void testInheritsBaseValidationRules() {
    // Test that it still validates base rules (like duplicate IDs)
    SerializedClassifierInstance root1 = createValidNode("duplicate", null);
    SerializedClassifierInstance root2 = createValidNode("duplicate", null);

    chunk.addClassifierInstances(root1, root2);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    // Should have multiple roots error
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("Duplicate node id: duplicate")));
  }

  @Test
  public void testAgainstDoubleRoots() {
    // Test that it still validates base rules (like duplicate IDs)
    SerializedClassifierInstance root1 = createValidNode("duplicate1", null);
    SerializedClassifierInstance root2 = createValidNode("duplicate2", null);

    chunk.addClassifierInstances(root1, root2);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    // Should have multiple roots error
    assertTrue(
        result.getIssues().stream()
            .anyMatch(error -> error.getMessage().contains("Expected exactly one root")));
  }

  @Test
  public void testComplexPartitionWithAnnotations() {
    SerializedClassifierInstance root = createValidNode("root", null);
    SerializedClassifierInstance child = createValidNode("child", "root");
    SerializedClassifierInstance annotation = createValidNode("annotation", "root");

    // Set up containment and annotation
    SerializedContainmentValue containment =
        new SerializedContainmentValue(testMetaPointer, "child");
    root.addContainmentValue(containment);

    root.addAnnotation("annotation");

    chunk.addClassifierInstances(root, child, annotation);
    chunk.addLanguages(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertTrue(result.isSuccessful());
    assertTrue(result.getIssues().isEmpty());
  }

  // Helper methods

  private SerializedClassifierInstance createValidNode(String id, String parentId) {
    SerializedClassifierInstance node = new SerializedClassifierInstance();
    node.setID(id);
    node.setParentNodeID(parentId);

    node.setClassifier(testMetaPointer);

    return node;
  }
}
