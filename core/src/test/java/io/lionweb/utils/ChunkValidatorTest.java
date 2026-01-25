package io.lionweb.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.serialization.data.*;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;

public class ChunkValidatorTest {

  private ChunkValidator validator;
  private SerializationChunk chunk;
  private LanguageVersion testLanguage;
  private MetaPointer testMetaPointer;

  @Before
  public void setUp() {
    validator = new ChunkValidator();
    chunk = new SerializationChunk();
    testLanguage = LanguageVersion.of("test-lang", "1.0");
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
    parent.unsafeAppendContainmentValue(containment);

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
    LanguageVersion differentLanguage = LanguageVersion.of("different-lang", "1.0");

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

    parent1.unsafeAppendContainmentValue(containment1);
    parent2.unsafeAppendContainmentValue(containment2);

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
    parent.unsafeAppendContainmentValue(containment);

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
    LanguageVersion propLanguage = LanguageVersion.of("prop-lang", "1.0");
    LanguageVersion refLanguage = LanguageVersion.of("ref-lang", "1.0");

    SerializedClassifierInstance node = createValidNode("node1", null);

    // Add property with different language
    SerializedPropertyValue property =
        SerializedPropertyValue.get(MetaPointer.get("prop-lang", "1.0", "TestProp"), "test value");
    node.unsafeAppendPropertyValue(property);

    // Add reference with different language
    SerializedReferenceValue reference =
        new SerializedReferenceValue(MetaPointer.get("ref-lang", "1.0", "TestRef"));
    reference.addValue(new SerializedReferenceValue.Entry("target1", "resolveInfo"));
    node.unsafeAppendReferenceValue(reference);

    chunk.addClassifierInstances(node);
    chunk.addLanguages(testLanguage, propLanguage, refLanguage);

    ValidationResult result = validator.validate(chunk);

    assertTrue(result.isSuccessful());
  }

  @Test
  public void testDuplicateContainmentMetaPointers() {
    SerializedClassifierInstance parent = createValidNode("parent1", null);
    SerializedClassifierInstance child1 = createValidNode("child1", "parent1");
    SerializedClassifierInstance child2 = createValidNode("child2", "parent1");

    // Create two containments with the same metapointer
    SerializedContainmentValue containment1 =
        new SerializedContainmentValue(testMetaPointer, Arrays.asList("child1"));
    SerializedContainmentValue containment2 =
        new SerializedContainmentValue(testMetaPointer, Arrays.asList("child2"));

    parent.unsafeAppendContainmentValue(containment1);
    parent.unsafeAppendContainmentValue(containment2);

    chunk.addClassifierInstances(parent, child1, child2);
    chunk.addLanguage(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error ->
                    error
                            .getMessage()
                            .contains("has duplicate feature metapointer: " + testMetaPointer)
                        && error.getMessage().contains("parent1")));
  }

  @Test
  public void testDuplicatePropertyMetaPointers() {
    SerializedClassifierInstance node = createValidNode("node1", null);

    // Create two properties with the same metapointer
    SerializedPropertyValue property1 = SerializedPropertyValue.get(testMetaPointer, "value1");
    SerializedPropertyValue property2 = SerializedPropertyValue.get(testMetaPointer, "value2");

    node.unsafeAppendPropertyValue(property1);
    node.unsafeAppendPropertyValue(property2);

    chunk.addClassifierInstances(node);
    chunk.addLanguage(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error ->
                    error
                            .getMessage()
                            .contains("has duplicate feature metapointer: " + testMetaPointer)
                        && error.getMessage().contains("node1")));
  }

  @Test
  public void testDuplicateReferenceMetaPointers() {
    SerializedClassifierInstance node = createValidNode("node1", null);

    // Create two references with the same metapointer
    SerializedReferenceValue reference1 = new SerializedReferenceValue(testMetaPointer);
    reference1.addValue(new SerializedReferenceValue.Entry("target1", "resolveInfo1"));

    SerializedReferenceValue reference2 = new SerializedReferenceValue(testMetaPointer);
    reference2.addValue(new SerializedReferenceValue.Entry("target2", "resolveInfo2"));

    node.unsafeAppendReferenceValue(reference1);
    node.unsafeAppendReferenceValue(reference2);

    chunk.addClassifierInstances(node);
    chunk.addLanguage(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    assertTrue(
        result.getIssues().stream()
            .anyMatch(
                error ->
                    error
                            .getMessage()
                            .contains("has duplicate feature metapointer: " + testMetaPointer)
                        && error.getMessage().contains("node1")));
  }

  @Test
  public void testMultipleDuplicateMetaPointers() {
    SerializedClassifierInstance node = createValidNode("node1", null);
    MetaPointer duplicatePointer = MetaPointer.get("test-lang", "1.0", "DuplicateFeature");

    // Add duplicate properties
    SerializedPropertyValue property1 = SerializedPropertyValue.get(duplicatePointer, "value1");
    SerializedPropertyValue property2 = SerializedPropertyValue.get(duplicatePointer, "value2");

    node.unsafeAppendPropertyValue(property1);
    node.unsafeAppendPropertyValue(property2);

    // Add duplicate references
    SerializedReferenceValue reference1 = new SerializedReferenceValue(duplicatePointer);
    reference1.addValue(new SerializedReferenceValue.Entry("target1", "resolveInfo1"));

    SerializedReferenceValue reference2 = new SerializedReferenceValue(duplicatePointer);
    reference2.addValue(new SerializedReferenceValue.Entry("target2", "resolveInfo2"));

    node.unsafeAppendReferenceValue(reference1);
    node.unsafeAppendReferenceValue(reference2);

    chunk.addClassifierInstances(node);
    chunk.addLanguage(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertFalse(result.isSuccessful());
    // Should detect both duplicate property and reference metapointers
    long duplicateErrors =
        result.getIssues().stream()
            .filter(
                error ->
                    error
                            .getMessage()
                            .contains("has duplicate feature metapointer: " + duplicatePointer)
                        && error.getMessage().contains("node1"))
            .count();

    // We expect 3 errors: one for properties, two for references
    assertEquals(3, duplicateErrors);
  }

  @Test
  public void testNoDuplicatesWhenDifferentMetaPointers() {
    SerializedClassifierInstance parent = createValidNode("parent1", null);
    SerializedClassifierInstance child1 = createValidNode("child1", "parent1");
    SerializedClassifierInstance child2 = createValidNode("child2", "parent1");

    // Create containments with different metapointers
    MetaPointer containmentPointer1 = MetaPointer.get("test-lang", "1.0", "Containment1");
    MetaPointer containmentPointer2 = MetaPointer.get("test-lang", "1.0", "Containment2");

    SerializedContainmentValue containment1 =
        new SerializedContainmentValue(containmentPointer1, Arrays.asList("child1"));
    SerializedContainmentValue containment2 =
        new SerializedContainmentValue(containmentPointer2, Arrays.asList("child2"));

    parent.unsafeAppendContainmentValue(containment1);
    parent.unsafeAppendContainmentValue(containment2);

    // Add properties and references with different metapointers
    MetaPointer propertyPointer1 = MetaPointer.get("test-lang", "1.0", "Property1");
    MetaPointer propertyPointer2 = MetaPointer.get("test-lang", "1.0", "Property2");

    SerializedPropertyValue property1 = SerializedPropertyValue.get(propertyPointer1, "value1");
    SerializedPropertyValue property2 = SerializedPropertyValue.get(propertyPointer2, "value2");

    parent.unsafeAppendPropertyValue(property1);
    parent.unsafeAppendPropertyValue(property2);

    MetaPointer referencePointer1 = MetaPointer.get("test-lang", "1.0", "Reference1");
    MetaPointer referencePointer2 = MetaPointer.get("test-lang", "1.0", "Reference2");

    SerializedReferenceValue reference1 = new SerializedReferenceValue(referencePointer1);
    reference1.addValue(new SerializedReferenceValue.Entry("target1", "resolveInfo1"));

    SerializedReferenceValue reference2 = new SerializedReferenceValue(referencePointer2);
    reference2.addValue(new SerializedReferenceValue.Entry("target2", "resolveInfo2"));

    parent.unsafeAppendReferenceValue(reference1);
    parent.unsafeAppendReferenceValue(reference2);

    chunk.addClassifierInstances(parent, child1, child2);
    chunk.addLanguage(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertTrue(result.isSuccessful());
    assertTrue(result.getIssues().isEmpty());
  }

  @Test
  public void testDuplicateMetaPointersAcrossDifferentNodes() {
    // Test that the same metapointer can be used in different nodes (should be valid)
    SerializedClassifierInstance node1 = createValidNode("node1", null);
    SerializedClassifierInstance node2 = createValidNode("node2", null);

    // Both nodes use the same metapointer for properties - this should be valid
    SerializedPropertyValue property1 = SerializedPropertyValue.get(testMetaPointer, "value1");
    SerializedPropertyValue property2 = SerializedPropertyValue.get(testMetaPointer, "value2");

    node1.unsafeAppendPropertyValue(property1);
    node2.unsafeAppendPropertyValue(property2);

    chunk.addClassifierInstances(node1, node2);
    chunk.addLanguage(testLanguage);

    ValidationResult result = validator.validate(chunk);

    assertTrue(result.isSuccessful());
    assertTrue(result.getIssues().isEmpty());
  }

  @Test
  public void testMultipleValidationErrors() {
    // Create nodes with multiple issues
    SerializedClassifierInstance invalidNode1 = createValidNode("", null); // Invalid ID
    SerializedClassifierInstance duplicateNode1 = createValidNode("dup", null);
    SerializedClassifierInstance duplicateNode2 = createValidNode("dup", null); // Duplicate ID

    chunk.addClassifierInstances(invalidNode1, duplicateNode1, duplicateNode2);
    chunk.addLanguages(LanguageVersion.of("wrong-lang", "1.0")); // Wrong language

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
