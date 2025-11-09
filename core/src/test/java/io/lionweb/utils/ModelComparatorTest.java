package io.lionweb.utils;

import static org.junit.Assert.*;

import io.lionweb.language.*;
import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ReferenceValue;
import io.lionweb.model.impl.DynamicAnnotationInstance;
import io.lionweb.model.impl.DynamicNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ModelComparatorTest {

  private Language createTestLanguage() {
    Language language = new Language("TestLanguage");
    return language;
  }

  private Concept createBasicConcept(Language language, String name) {
    Concept concept = new Concept(language, name, name + "-id");
    concept.addProperty("name", LionCoreBuiltins.getString());
    return concept;
  }

  private Annotation createBasicAnnotation(Language language, String name) {
    Annotation annotation = new Annotation(language, name, name + "-ann-id");
    annotation.addProperty("value", LionCoreBuiltins.getString());
    return annotation;
  }

  @Test
  public void comparisonResultEmptyIsEquivalent() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    assertTrue(result.areEquivalent());
    assertTrue(result.getDifferences().isEmpty());
  }

  @Test
  public void comparisonResultWithDifferencesIsNotEquivalent() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentIDs("context", "id1", "id2");
    assertFalse(result.areEquivalent());
    assertEquals(1, result.getDifferences().size());
    assertEquals("context: different ids, a=id1, b=id2", result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultMarkDifferentIDs() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentIDs("root", "nodeA", "nodeB");
    assertEquals("root: different ids, a=nodeA, b=nodeB", result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultMarkDifferentAnnotated() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentAnnotated("context", "annA", "annB");
    assertEquals(
        "context: different annotated ids, a=annA, b=annB", result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultMarkDifferentConcept() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentConcept("context", "nodeId", "conceptA", "conceptB");
    assertEquals(
        "context (id=nodeId) : different concepts, a=conceptA, b=conceptB",
        result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultMarkDifferentPropertyValue() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentPropertyValue("context", "nodeId", "propName", "valueA", "valueB");
    assertEquals(
        "context (id=nodeId) : different property value for propName, a=valueA, b=valueB",
        result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultMarkDifferentNumberOfChildren() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentNumberOfChildren("context", "nodeId", "containment", 2, 3);
    assertEquals(
        "context (id=nodeId) : different number of children for containment, a=2, b=3",
        result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultMarkDifferentNumberOfReferences() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentNumberOfReferences("context", "nodeId", "reference", 1, 2);
    assertEquals(
        "context (id=nodeId) : different number of referred for reference, a=1, b=2",
        result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultMarkDifferentReferredID() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentReferredID("context", "nodeId", "refName", 0, "refA", "refB");
    assertEquals(
        "context (id=nodeId) : different referred id for refName index 0, a=refA, b=refB",
        result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultMarkDifferentResolveInfo() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentResolveInfo("context", "nodeId", "refName", 1, "infoA", "infoB");
    assertEquals(
        "context (id=nodeId) : different resolve info for refName index 1, a=infoA, b=infoB",
        result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultMarkIncompatible() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markIncompatible();
    assertEquals("incompatible instances", result.getDifferences().get(0));
    assertFalse(result.areEquivalent());
  }

  @Test
  public void comparisonResultMarkDifferentNumberOfAnnotations() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentNumberOfAnnotations("context", 2, 3);
    assertEquals(
        "context different number of annotations (2 != 3)", result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultMarkDifferentAnnotation() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentAnnotation("context", 1);
    assertEquals("context annotation 1 is different", result.getDifferences().get(0));
  }

  @Test
  public void comparisonResultToString() {
    ModelComparator.ComparisonResult result = new ModelComparator.ComparisonResult();
    result.markDifferentIDs("context", "id1", "id2");
    String toString = result.toString();
    assertTrue(toString.contains("ComparisonResult"));
    assertTrue(toString.contains("different ids"));
  }

  @Test
  public void areEquivalentIdenticalNodes() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node1", concept);
    node1.setPropertyValue(concept.getPropertyByName("name"), "test");
    node2.setPropertyValue(concept.getPropertyByName("name"), "test");

    assertTrue(ModelComparator.areEquivalent(node1, node2));
  }

  @Test
  public void areEquivalentDifferentNodes() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node2", concept);

    assertFalse(ModelComparator.areEquivalent(node1, node2));
  }

  @Test
  public void areEquivalentListsSameSize() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1a = new DynamicNode("node1", concept);
    DynamicNode node1b = new DynamicNode("node1", concept);
    DynamicNode node2a = new DynamicNode("node2", concept);
    DynamicNode node2b = new DynamicNode("node2", concept);

    List<DynamicNode> listA = Arrays.asList(node1a, node2a);
    List<DynamicNode> listB = Arrays.asList(node1b, node2b);

    assertTrue(ModelComparator.areEquivalent(listA, listB));
  }

  @Test
  public void areEquivalentListsDifferentSizes() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node2", concept);

    List<DynamicNode> listA = Arrays.asList(node1);
    List<DynamicNode> listB = Arrays.asList(node1, node2);

    assertFalse(ModelComparator.areEquivalent(listA, listB));
  }

  @Test
  public void areEquivalentListsWithDifferentElements() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node2", concept);
    DynamicNode node3 = new DynamicNode("node3", concept);

    List<DynamicNode> listA = Arrays.asList(node1, node2);
    List<DynamicNode> listB = Arrays.asList(node1, node3);

    assertFalse(ModelComparator.areEquivalent(listA, listB));
  }

  @Test
  public void compareNodesIdentical() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node1", concept);

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(node1, node2);

    assertTrue(result.areEquivalent());
  }

  @Test
  public void compareNodesDifferentIDs() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node2", concept);

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(node1, node2);

    assertFalse(result.areEquivalent());
    assertEquals(1, result.getDifferences().size());
    assertTrue(result.getDifferences().get(0).contains("different ids"));
  }

  @Test
  public void compareNodesDifferentConcepts() {
    Language language = createTestLanguage();
    Concept concept1 = createBasicConcept(language, "Concept1");
    Concept concept2 = createBasicConcept(language, "Concept2");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept1);
    DynamicNode node2 = new DynamicNode("node1", concept2);

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(node1, node2);

    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("different concepts"));
  }

  @Test
  public void compareNodesDifferentPropertyValues() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node1", concept);

    Property nameProperty = concept.getPropertyByName("name");
    node1.setPropertyValue(nameProperty, "value1");
    node2.setPropertyValue(nameProperty, "value2");

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(node1, node2);

    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("different property value"));
    assertTrue(result.getDifferences().get(0).contains("value1"));
    assertTrue(result.getDifferences().get(0).contains("value2"));
  }

  @Test
  public void compareNodesWithDifferentNumberOfChildren() {
    Language language = createTestLanguage();
    Concept parentConcept = new Concept(language, "Parent", "parent-id");
    Concept childConcept = new Concept(language, "Child", "child-id");
    parentConcept.addContainment("children", childConcept, Multiplicity.ZERO_OR_MORE);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode parent1 = new DynamicNode("parent1", parentConcept);
    DynamicNode parent2 = new DynamicNode("parent1", parentConcept);
    DynamicNode child1 = new DynamicNode("child1", childConcept);
    DynamicNode child2 = new DynamicNode("child2", childConcept);

    parent1.addChild(parentConcept.getContainmentByName("children"), child1);
    parent2.addChild(parentConcept.getContainmentByName("children"), child1);
    parent2.addChild(parentConcept.getContainmentByName("children"), child2);

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(parent1, parent2);

    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("different number of children"));
  }

  @Test
  public void compareNodesWithDifferentNumberOfReferences() {
    Language language = createTestLanguage();
    Concept sourceConcept = new Concept(language, "Source", "source-id");
    Concept targetConcept = new Concept(language, "Target", "target-id");
    Reference reference = new Reference();
    reference.setName("targets");
    reference.setType(targetConcept);
    reference.setMultiple(true);
    sourceConcept.addFeature(reference);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode source1 = new DynamicNode("source1", sourceConcept);
    DynamicNode source2 = new DynamicNode("source1", sourceConcept);
    DynamicNode target1 = new DynamicNode("target1", targetConcept);
    DynamicNode target2 = new DynamicNode("target2", targetConcept);

    source1.setReferenceValues(reference, Arrays.asList(new ReferenceValue(target1, null)));
    source2.setReferenceValues(
        reference,
        Arrays.asList(new ReferenceValue(target1, null), new ReferenceValue(target2, null)));

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(source1, source2);

    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("different number of referred"));
  }

  @Test
  public void compareNodesWithDifferentReferredIDs() {
    Language language = createTestLanguage();
    Concept sourceConcept = new Concept(language, "Source", "source-id");
    Concept targetConcept = new Concept(language, "Target", "target-id");
    Reference reference = new Reference();
    reference.setName("target");
    reference.setType(targetConcept);
    reference.setMultiple(false);
    sourceConcept.addFeature(reference);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode source1 = new DynamicNode("source1", sourceConcept);
    DynamicNode source2 = new DynamicNode("source1", sourceConcept);
    DynamicNode target1 = new DynamicNode("target1", targetConcept);
    DynamicNode target2 = new DynamicNode("target2", targetConcept);

    source1.setReferenceValues(reference, Arrays.asList(new ReferenceValue(target1, null)));
    source2.setReferenceValues(reference, Arrays.asList(new ReferenceValue(target2, null)));

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(source1, source2);

    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("different referred id"));
  }

  @Test
  public void compareNodesWithDifferentResolveInfo() {
    Language language = createTestLanguage();
    Concept sourceConcept = new Concept(language, "Source", "source-id");
    Concept targetConcept = new Concept(language, "Target", "target-id");
    Reference reference = new Reference();
    reference.setName("target");
    reference.setType(targetConcept);
    reference.setMultiple(false);
    sourceConcept.addFeature(reference);
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode source1 = new DynamicNode("source1", sourceConcept);
    DynamicNode source2 = new DynamicNode("source1", sourceConcept);
    DynamicNode target = new DynamicNode("target1", targetConcept);

    source1.setReferenceValues(reference, Arrays.asList(new ReferenceValue(target, "info1")));
    source2.setReferenceValues(reference, Arrays.asList(new ReferenceValue(target, "info2")));

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(source1, source2);

    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("different resolve info"));
  }

  @Test
  public void compareAnnotationInstancesIdentical() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    Annotation annotation = createBasicAnnotation(language, "TestAnnotation");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node = new DynamicNode("node1", concept);
    DynamicAnnotationInstance ann1 = new DynamicAnnotationInstance("ann1", annotation, node);
    DynamicAnnotationInstance ann2 = new DynamicAnnotationInstance("ann1", annotation, node);

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(ann1, ann2);

    assertTrue(result.areEquivalent());
  }

  @Test
  public void compareAnnotationInstancesDifferentParents() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    Annotation annotation = createBasicAnnotation(language, "TestAnnotation");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node2", concept);
    DynamicAnnotationInstance ann1 = new DynamicAnnotationInstance("ann1", annotation, node1);
    DynamicAnnotationInstance ann2 = new DynamicAnnotationInstance("ann1", annotation, node2);

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(ann1, ann2);

    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("different annotated ids"));
  }

  @Test
  public void compareClassifierInstancesIncompatibleTypes() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    Annotation annotation = createBasicAnnotation(language, "TestAnnotation");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node = new DynamicNode("node1", concept);
    DynamicAnnotationInstance ann = new DynamicAnnotationInstance("ann1", annotation, node);

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(node, ann);

    assertFalse(result.areEquivalent());
    assertEquals("incompatible instances", result.getDifferences().get(0));
  }

  @Test
  public void compareNodesWithDifferentAnnotations() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    Annotation annotation = createBasicAnnotation(language, "TestAnnotation");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node1", concept);
    DynamicAnnotationInstance ann1 = new DynamicAnnotationInstance("ann1", annotation, node1);
    DynamicAnnotationInstance ann2 = new DynamicAnnotationInstance("ann2", annotation, node2);

    node1.addAnnotation(ann1);
    node2.addAnnotation(ann2);

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(node1, node2);

    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("annotation 0 is different"));
  }

  @Test
  public void compareNodesWithDifferentNumberOfAnnotations() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    Annotation annotation = createBasicAnnotation(language, "TestAnnotation");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node1", concept);
    DynamicAnnotationInstance ann1 = new DynamicAnnotationInstance("ann1", annotation, node1);
    DynamicAnnotationInstance ann2 = new DynamicAnnotationInstance("ann2", annotation, node2);

    node1.addAnnotation(ann1);
    node2.addAnnotation(ann1);
    node2.addAnnotation(ann2);

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(node1, node2);

    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("different number of annotations"));
  }

  @Test
  public void compareDeepNodeHierarchy() {
    Language language = createTestLanguage();
    Concept parentConcept = new Concept(language, "Parent", "parent-id");
    Concept childConcept = new Concept(language, "Child", "child-id");
    parentConcept.addContainment("child", childConcept, Multiplicity.OPTIONAL);
    childConcept.addContainment("grandchild", childConcept, Multiplicity.OPTIONAL);
    childConcept.addProperty("name", LionCoreBuiltins.getString());
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    // Create first hierarchy
    DynamicNode parent1 = new DynamicNode("parent1", parentConcept);
    DynamicNode child1 = new DynamicNode("child1", childConcept);
    DynamicNode grandchild1 = new DynamicNode("grandchild1", childConcept);
    child1.setPropertyValue(childConcept.getPropertyByName("name"), "child1");
    grandchild1.setPropertyValue(childConcept.getPropertyByName("name"), "grandchild1");
    child1.addChild(childConcept.getContainmentByName("grandchild"), grandchild1);
    parent1.addChild(parentConcept.getContainmentByName("child"), child1);

    // Create second hierarchy with different grandchild property
    DynamicNode parent2 = new DynamicNode("parent1", parentConcept);
    DynamicNode child2 = new DynamicNode("child1", childConcept);
    DynamicNode grandchild2 = new DynamicNode("grandchild1", childConcept);
    child2.setPropertyValue(childConcept.getPropertyByName("name"), "child1");
    grandchild2.setPropertyValue(childConcept.getPropertyByName("name"), "different_name");
    child2.addChild(childConcept.getContainmentByName("grandchild"), grandchild2);
    parent2.addChild(parentConcept.getContainmentByName("child"), child2);

    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(parent1, parent2);

    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("different property value"));
    assertTrue(result.getDifferences().get(0).contains("/child[0]/grandchild[0]"));
  }

  @Test
  public void compareEmptyLists() {
    List<ClassifierInstance<?>> emptyList1 = Collections.emptyList();
    List<ClassifierInstance<?>> emptyList2 = Collections.emptyList();

    assertTrue(ModelComparator.areEquivalent(emptyList1, emptyList2));
  }

  @Test
  public void compareNullPropertyValues() {
    Language language = createTestLanguage();
    Concept concept = createBasicConcept(language, "TestConcept");
    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    DynamicNode node1 = new DynamicNode("node1", concept);
    DynamicNode node2 = new DynamicNode("node1", concept);

    // Both have null values - should be equivalent
    ModelComparator comparator = new ModelComparator();
    ModelComparator.ComparisonResult result = comparator.compare(node1, node2);
    assertTrue(result.areEquivalent());

    // One has null, other has value - should be different
    Property nameProperty = concept.getPropertyByName("name");
    node2.setPropertyValue(nameProperty, "value");
    result = comparator.compare(node1, node2);
    assertFalse(result.areEquivalent());
    assertTrue(result.getDifferences().get(0).contains("different property value"));
  }
}
