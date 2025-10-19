package io.lionweb.model;

import static org.junit.Assert.*;

import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.impl.DynamicNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ClassifierInstanceUtilsTest {

  @Test
  public void setPropertyValueByNamePositive() {
    // Create a language and concept with a property
    Language testLanguage = new Language("MyTestLanguage");
    Concept testConcept = new Concept(testLanguage, "TestConcept");
    testConcept.addProperty("testProp", LionCoreBuiltins.getString());

    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create a node instance
    DynamicNode node = new DynamicNode("node1", testConcept);

    // Set property value by name
    ClassifierInstanceUtils.setPropertyValueByName(node, "testProp", "test value");

    // Verify the value was set
    assertEquals("test value", ClassifierInstanceUtils.getPropertyValueByName(node, "testProp"));
  }

  @Test
  public void setPropertyValueByNameNonExistentProperty() {
    // Create a language and concept
    Language testLanguage = new Language("MyTestLanguage");
    Concept testConcept = new Concept(testLanguage, "TestConcept");

    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create a node instance
    DynamicNode node = new DynamicNode("node1", testConcept);

    // Try to set a property that doesn't exist
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> ClassifierInstanceUtils.setPropertyValueByName(node, "nonExistentProp", "value"));

    assertTrue(
        exception.getMessage().contains("does not contained a property named nonExistentProp"));
  }

  @Test
  public void setPropertyValueByNameNullInstance() {
    assertThrows(
        NullPointerException.class,
        () -> ClassifierInstanceUtils.setPropertyValueByName(null, "prop", "value"));
  }

  @Test
  public void setPropertyValueByNameNullPropertyName() {
    Language testLanguage = new Language("MyTestLanguage");
    Concept testConcept = new Concept(testLanguage, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    DynamicNode node = new DynamicNode("node1", testConcept);

    assertThrows(
        NullPointerException.class,
        () -> ClassifierInstanceUtils.setPropertyValueByName(node, null, "value"));
  }

  @Test
  public void getPropertyValueByIDPositive() {
    // Create a language and concept with a property
    Language testLanguage = new Language("MyTestLanguage");
    Concept testConcept = new Concept(testLanguage, "TestConcept");
    Property prop = Property.createRequired("testProp", LionCoreBuiltins.getString());
    prop.setID("test-prop-id");
    testConcept.addFeature(prop);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create a node instance and set a property value
    DynamicNode node = new DynamicNode("node1", testConcept);
    node.setPropertyValue(prop, "test value");

    // Get property value by ID
    Object value = ClassifierInstanceUtils.getPropertyValueByID(node, "test-prop-id");
    assertEquals("test value", value);
  }

  @Test
  public void getPropertyValueByIDNonExistentProperty() {
    Language testLanguage = new Language("MyTestLanguage");
    Concept testConcept = new Concept(testLanguage, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    DynamicNode node = new DynamicNode("node1", testConcept);

    // Try to get property by non-existent ID - this causes an exception to be thrown
    assertThrows(
        IllegalArgumentException.class,
        () -> ClassifierInstanceUtils.getPropertyValueByID(node, "non-existent-id"));
  }

  @Test
  public void getPropertyValueByIDNullInstance() {
    assertThrows(
        NullPointerException.class,
        () -> {
          ClassifierInstanceUtils.getPropertyValueByID(null, "prop-id");
        });
  }

  @Test
  public void getPropertyValueByIDNullPropertyID() {
    Language testLanguage = new Language("MyTestLanguage");
    Concept testConcept = new Concept(testLanguage, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    DynamicNode node = new DynamicNode("node1", testConcept);

    assertThrows(
        NullPointerException.class, () -> ClassifierInstanceUtils.getPropertyValueByID(node, null));
  }

  @Test
  public void getOnlyChildByContainmentNameWithSingleChild() {
    // Create language and concepts
    Language testLanguage = new Language("MyTestLanguage");
    Concept parentConcept = new Concept(testLanguage, "ParentConcept");
    Concept childConcept = new Concept(testLanguage, "ChildConcept");
    parentConcept.addContainment("child", childConcept, Multiplicity.OPTIONAL);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create nodes
    DynamicNode parentNode = new DynamicNode("parent", parentConcept);
    DynamicNode childNode = new DynamicNode("child", childConcept);

    // Add child
    ClassifierInstanceUtils.addChild(parentNode, "child", childNode);

    // Get only child
    Node retrievedChild =
        ClassifierInstanceUtils.getOnlyChildByContainmentName(parentNode, "child");
    assertEquals(childNode, retrievedChild);
  }

  @Test
  public void getOnlyChildByContainmentNameWithNoChild() {
    // Create language and concepts
    Language testLanguage = new Language("MyTestLanguage");
    Concept parentConcept = new Concept(testLanguage, "ParentConcept");
    Concept childConcept = new Concept(testLanguage, "ChildConcept");
    parentConcept.addContainment("child", childConcept, Multiplicity.OPTIONAL);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create parent node without children
    DynamicNode parentNode = new DynamicNode("parent", parentConcept);

    // Get only child - should return null
    Node retrievedChild =
        ClassifierInstanceUtils.getOnlyChildByContainmentName(parentNode, "child");
    assertNull(retrievedChild);
  }

  @Test
  public void getOnlyChildByContainmentNameWithMultipleChildren() {
    // Create language and concepts
    Language testLanguage = new Language("MyTestLanguage");
    Concept parentConcept = new Concept(testLanguage, "ParentConcept");
    Concept childConcept = new Concept(testLanguage, "ChildConcept");
    parentConcept.addContainment("children", childConcept, Multiplicity.ZERO_OR_MORE);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create nodes
    DynamicNode parentNode = new DynamicNode("parent", parentConcept);
    DynamicNode childNode1 = new DynamicNode("child1", childConcept);
    DynamicNode childNode2 = new DynamicNode("child2", childConcept);

    // Add multiple children
    ClassifierInstanceUtils.addChild(parentNode, "children", childNode1);
    ClassifierInstanceUtils.addChild(parentNode, "children", childNode2);

    // Get only child - should throw IllegalStateException
    assertThrows(
        IllegalStateException.class,
        () -> ClassifierInstanceUtils.getOnlyChildByContainmentName(parentNode, "children"));
  }

  @Test
  public void setOnlyChildByContainmentNameAddNew() {
    // Create language and concepts
    Language testLanguage = new Language("MyTestLanguage");
    Concept parentConcept = new Concept(testLanguage, "ParentConcept");
    Concept childConcept = new Concept(testLanguage, "ChildConcept");
    parentConcept.addContainment("child", childConcept, Multiplicity.OPTIONAL);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create nodes
    DynamicNode parentNode = new DynamicNode("parent", parentConcept);
    DynamicNode childNode = new DynamicNode("child", childConcept);

    // Set only child
    ClassifierInstanceUtils.setOnlyChildByContainmentName(parentNode, "child", childNode);

    // Verify child was set
    Node retrievedChild =
        ClassifierInstanceUtils.getOnlyChildByContainmentName(parentNode, "child");
    assertEquals(childNode, retrievedChild);
  }

  @Test
  public void setOnlyChildByContainmentNameReplaceExisting() {
    // Create language and concepts
    Language testLanguage = new Language("MyTestLanguage");
    Concept parentConcept = new Concept(testLanguage, "ParentConcept");
    Concept childConcept = new Concept(testLanguage, "ChildConcept");
    parentConcept.addContainment("child", childConcept, Multiplicity.OPTIONAL);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create nodes
    DynamicNode parentNode = new DynamicNode("parent", parentConcept);
    DynamicNode oldChild = new DynamicNode("oldChild", childConcept);
    DynamicNode newChild = new DynamicNode("newChild", childConcept);

    // Set initial child
    ClassifierInstanceUtils.setOnlyChildByContainmentName(parentNode, "child", oldChild);
    assertEquals(
        oldChild, ClassifierInstanceUtils.getOnlyChildByContainmentName(parentNode, "child"));

    // Replace with new child
    ClassifierInstanceUtils.setOnlyChildByContainmentName(parentNode, "child", newChild);
    assertEquals(
        newChild, ClassifierInstanceUtils.getOnlyChildByContainmentName(parentNode, "child"));

    // Verify old child is no longer present
    List<? extends Node> children =
        ClassifierInstanceUtils.getChildrenByContainmentName(parentNode, "child");
    assertEquals(1, children.size());
    assertEquals(newChild, children.get(0));
  }

  @Test
  public void setOnlyChildByContainmentNameWithMultipleContainment() {
    // Create language and concepts
    Language testLanguage = new Language("MyTestLanguage");
    Concept parentConcept = new Concept(testLanguage, "ParentConcept");
    Concept childConcept = new Concept(testLanguage, "ChildConcept");
    parentConcept.addContainment("children", childConcept, Multiplicity.ZERO_OR_MORE);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create nodes
    DynamicNode parentNode = new DynamicNode("parent", parentConcept);
    DynamicNode childNode = new DynamicNode("child", childConcept);

    // Try to set only child on a multiple containment - should throw IllegalArgumentException
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                ClassifierInstanceUtils.setOnlyChildByContainmentName(
                    parentNode, "children", childNode));

    assertTrue(
        exception.getMessage().contains("Cannot invoke this method with a multiple containment"));
  }

  @Test
  public void setOnlyChildByContainmentNameSetNull() {
    // Create language and concepts
    Language testLanguage = new Language("MyTestLanguage");
    Concept parentConcept = new Concept(testLanguage, "ParentConcept");
    Concept childConcept = new Concept(testLanguage, "ChildConcept");
    parentConcept.addContainment("child", childConcept, Multiplicity.OPTIONAL);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create nodes and set initial child
    DynamicNode parentNode = new DynamicNode("parent", parentConcept);
    DynamicNode childNode = new DynamicNode("child", childConcept);
    ClassifierInstanceUtils.setOnlyChildByContainmentName(parentNode, "child", childNode);

    // Set child to null (remove it)
    ClassifierInstanceUtils.setOnlyChildByContainmentName(parentNode, "child", null);

    // Verify child was removed
    assertNull(ClassifierInstanceUtils.getOnlyChildByContainmentName(parentNode, "child"));
  }

  @Test
  public void setOnlyReferenceValuePositive() {
    // Create language and concepts with reference
    Language testLanguage = new Language("MyTestLanguage");
    Concept sourceConcept = new Concept(testLanguage, "SourceConcept");
    Concept targetConcept = new Concept(testLanguage, "TargetConcept");
    Reference ref = new Reference();
    ref.setName("targetRef");
    ref.setType(targetConcept);
    ref.setMultiple(false);
    sourceConcept.addFeature(ref);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create nodes
    DynamicNode sourceNode = new DynamicNode("source", sourceConcept);
    DynamicNode targetNode = new DynamicNode("target", targetConcept);

    // Set only reference value
    ClassifierInstanceUtils.setOnlyReferenceValue(
        sourceNode, ref, new ReferenceValue(targetNode, null));

    // Verify reference was set
    List<? extends Node> referredNodes = ClassifierInstanceUtils.getReferredNodes(sourceNode, ref);
    assertEquals(1, referredNodes.size());
    assertEquals(targetNode, referredNodes.get(0));
  }

  @Test
  public void setOnlyReferenceValueByNamePositive() {
    // Create language and concepts with reference
    Language testLanguage = new Language("MyTestLanguage");
    Concept sourceConcept = new Concept(testLanguage, "SourceConcept");
    Concept targetConcept = new Concept(testLanguage, "TargetConcept");
    Reference ref = new Reference();
    ref.setName("targetRef");
    ref.setType(targetConcept);
    ref.setMultiple(false);
    sourceConcept.addFeature(ref);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create nodes
    DynamicNode sourceNode = new DynamicNode("source", sourceConcept);
    DynamicNode targetNode = new DynamicNode("target", targetConcept);

    // Set only reference value by name
    ClassifierInstanceUtils.setOnlyReferenceValueByName(
        sourceNode, "targetRef", new ReferenceValue(targetNode, null));

    // Verify reference was set
    List<? extends Node> referredNodes = ClassifierInstanceUtils.getReferredNodes(sourceNode, ref);
    assertEquals(1, referredNodes.size());
    assertEquals(targetNode, referredNodes.get(0));
  }

  @Test
  public void setReferenceValuesByNamePositive() {
    // Create language and concepts with multiple reference
    Language testLanguage = new Language("MyTestLanguage");
    Concept sourceConcept = new Concept(testLanguage, "SourceConcept");
    Concept targetConcept = new Concept(testLanguage, "TargetConcept");
    Reference ref = new Reference();
    ref.setName("targetRefs");
    ref.setType(targetConcept);
    ref.setMultiple(true);
    sourceConcept.addFeature(ref);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create nodes
    DynamicNode sourceNode = new DynamicNode("source", sourceConcept);
    DynamicNode targetNode1 = new DynamicNode("target1", targetConcept);
    DynamicNode targetNode2 = new DynamicNode("target2", targetConcept);

    // Set multiple reference values by name
    List<ReferenceValue> targets =
        Arrays.asList(new ReferenceValue(targetNode1, null), new ReferenceValue(targetNode2, null));
    ClassifierInstanceUtils.setReferenceValuesByName(sourceNode, "targetRefs", targets);

    // Verify references were set
    List<? extends Node> referredNodes = ClassifierInstanceUtils.getReferredNodes(sourceNode, ref);
    assertEquals(2, referredNodes.size());
    assertTrue(referredNodes.contains(targetNode1));
    assertTrue(referredNodes.contains(targetNode2));
  }

  @Test
  public void setReferenceValuesByNameEmptyList() {
    // Create language and concepts with multiple reference
    Language testLanguage = new Language("MyTestLanguage");
    Concept sourceConcept = new Concept(testLanguage, "SourceConcept");
    Concept targetConcept = new Concept(testLanguage, "TargetConcept");
    Reference ref = new Reference();
    ref.setName("targetRefs");
    ref.setType(targetConcept);
    ref.setMultiple(true);
    sourceConcept.addFeature(ref);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create source node
    DynamicNode sourceNode = new DynamicNode("source", sourceConcept);

    // Set empty reference values
    ClassifierInstanceUtils.setReferenceValuesByName(
        sourceNode, "targetRefs", Collections.emptyList());

    // Verify no references were set
    List<? extends Node> referredNodes = ClassifierInstanceUtils.getReferredNodes(sourceNode, ref);
    assertEquals(0, referredNodes.size());
  }

  @Test
  public void setReferenceValuesByNameNullList() {
    // Create language and concepts with multiple reference
    Language testLanguage = new Language("MyTestLanguage");
    Concept sourceConcept = new Concept(testLanguage, "SourceConcept");
    Concept targetConcept = new Concept(testLanguage, "TargetConcept");
    Reference ref = new Reference();
    ref.setName("targetRefs");
    ref.setType(targetConcept);
    ref.setMultiple(true);
    sourceConcept.addFeature(ref);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create source node
    DynamicNode sourceNode = new DynamicNode("source", sourceConcept);

    // Set null reference values
    assertThrows(
        NullPointerException.class,
        () -> ClassifierInstanceUtils.setReferenceValuesByName(sourceNode, "targetRefs", null));
  }

  @Test
  public void getReferenceValueByNameNullClassifier() {
    // Create a mock ClassifierInstance with null classifier
    ClassifierInstance<?> mockInstance = new DynamicNode("mockNode", null);

    // Try to get reference value with null classifier
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> ClassifierInstanceUtils.getReferenceValueByName(mockInstance, "refName"));

    assertTrue(exception.getMessage().contains("Concept should not be null"));
  }

  @Test
  public void getReferenceValueByNameNonExistentReferenceDetailedMessage() {
    // Create language and concept without any references
    Language testLanguage = new Language("MyTestLanguage");
    Concept sourceConcept = new Concept(testLanguage, "SourceConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(testLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(testLanguage);

    // Create source node
    DynamicNode sourceNode = new DynamicNode("source", sourceConcept);

    // Try to get reference by non-existent name and verify the exact error message
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> ClassifierInstanceUtils.getReferenceValueByName(sourceNode, "nonExistentRef"));

    // Verify the complete error message structure
    String expectedMessage =
        "Concept "
            + sourceConcept.qualifiedName()
            + " does not contain a property named nonExistentRef";
    assertEquals(expectedMessage, exception.getMessage());
  }

  @Test
  public void isBuiltinElementWithTheRealDeal() {
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(LionCore.getInstance(LionWebVersion.v2023_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(LionCore.getClassifier(LionWebVersion.v2023_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(LionCore.getAnnotation(LionWebVersion.v2023_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(LionCore.getInterface(LionWebVersion.v2023_1)));

    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(LionCore.getInstance(LionWebVersion.v2024_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(LionCore.getClassifier(LionWebVersion.v2024_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(LionCore.getAnnotation(LionWebVersion.v2024_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(LionCore.getInterface(LionWebVersion.v2024_1)));

    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(
            LionCoreBuiltins.getInstance(LionWebVersion.v2023_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(
            LionCoreBuiltins.getInteger(LionWebVersion.v2023_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(LionCoreBuiltins.getNode(LionWebVersion.v2023_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(
            LionCoreBuiltins.getINamed(LionWebVersion.v2023_1)));

    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(
            LionCoreBuiltins.getInstance(LionWebVersion.v2024_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(
            LionCoreBuiltins.getInteger(LionWebVersion.v2024_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(LionCoreBuiltins.getNode(LionWebVersion.v2024_1)));
    assertTrue(
        ClassifierInstanceUtils.isBuiltinElement(
            LionCoreBuiltins.getINamed(LionWebVersion.v2024_1)));
  }

  @Test
  public void isBuiltinElementFalseCase() {
    Language aLanguage = new Language("MyTestLanguage");
    Concept aConcept = new Concept(aLanguage, "MyTestConcept");
    assertFalse(ClassifierInstanceUtils.isBuiltinElement(aLanguage));
    assertFalse(ClassifierInstanceUtils.isBuiltinElement(aConcept));
  }

  @Test
  public void isBuiltinElementToNewConceptInLionCore() {
    Language original = LionCore.getInstance(LionWebVersion.v2024_1);
    Language lionCoreM3 =
        new Language(original.getName(), "ID1", original.getKey(), original.getVersion());
    Concept testConcept = new Concept(lionCoreM3, "TestConcept");
    assertTrue(ClassifierInstanceUtils.isBuiltinElement(testConcept));
  }

  @Test
  public void isBuiltinElementToNewConceptInLionCoreBuiltins() {
    Language original = LionCoreBuiltins.getInstance(LionWebVersion.v2024_1);
    Language lionCoreBuiltins =
        new Language(original.getName(), "ID1", original.getKey(), original.getVersion());
    Concept testConcept = new Concept(lionCoreBuiltins, "TestConcept");
    assertTrue(ClassifierInstanceUtils.isBuiltinElement(testConcept));
  }
}
