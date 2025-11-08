package io.lionweb.model.impl;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.MockPartitionObserver;
import io.lionweb.model.Node;
import io.lionweb.model.ReferenceValue;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class M3NodeTest {

  @Test
  public void toStringEnumerationLiteralWithoutId() {
    EnumerationLiteral literal = new EnumerationLiteral();
    assertEquals("EnumerationLiteral[null]", literal.toString());
  }

  @Test
  public void toStringEnumerationLiteralIncludingId() {
    EnumerationLiteral literal = new EnumerationLiteral();
    literal.setID("123");
    assertEquals("EnumerationLiteral[123]", literal.toString());
  }

  @Test
  public void toStringContainmentWithoutId() {
    Containment containment = new Containment();
    assertEquals(
        "Containment[null]{qualifiedName=<no language>.<unnamed>, type=null}",
        containment.toString());
  }

  @Test
  public void toStringContainmentIncludingId() {
    Containment containment = new Containment();
    containment.setID("asdf");
    assertEquals(
        "Containment[asdf]{qualifiedName=<no language>.<unnamed>, type=null}",
        containment.toString());
  }

  @Test
  public void observer() {
    Language language = new Language();
    language.setID("l1");
    MockPartitionObserver observer = new MockPartitionObserver();
    language.registerPartitionObserver(observer);

    // propertyChanged
    language.setName("MyLanguage");
    language.setName("MyOtherLanguage");
    Property name = LionCoreBuiltins.getINamed().getPropertyByName("name");
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.PropertyChangedRecord(language, name, null, "MyLanguage"),
            new MockPartitionObserver.PropertyChangedRecord(
                language, name, "MyLanguage", "MyOtherLanguage")),
        observer.getRecords());
    observer.clearRecords();

    // childAdded
    Concept c1 = new Concept();
    c1.setID("c1");
    Concept c2 = new Concept();
    c2.setID("c2");
    language.addElement(c1);
    language.addElement(c2);
    Containment entities = LionCore.getLanguage().getContainmentByName("entities");
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ChildAddedRecord(language, entities, 0, c1),
            new MockPartitionObserver.ChildAddedRecord(language, entities, 1, c2)),
        observer.getRecords());
    observer.clearRecords();

    // childRemoved
    language.removeChild(c2);
    language.removeChild(c1);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ChildRemovedRecord(language, entities, 1, c2),
            new MockPartitionObserver.ChildRemovedRecord(language, entities, 0, c1)),
        observer.getRecords());
    observer.clearRecords();

    // annotationAdded
    Annotation annotation = new Annotation();
    annotation.setID("a1");
    AnnotationInstance ann1 = new DynamicAnnotationInstance("ai1", annotation);
    AnnotationInstance ann2 = new DynamicAnnotationInstance("ai2", annotation);
    language.addAnnotation(ann1);
    language.addAnnotation(ann2);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.AnnotationAddedRecord(language, 0, ann1),
            new MockPartitionObserver.AnnotationAddedRecord(language, 1, ann2)),
        observer.getRecords());
    observer.clearRecords();

    // annotationRemoved
    language.removeAnnotation(ann2);
    language.removeAnnotation(ann1);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.AnnotationRemovedRecord(language, 1, ann2),
            new MockPartitionObserver.AnnotationRemovedRecord(language, 0, ann1)),
        observer.getRecords());
    observer.clearRecords();

    // referenceValueAdded
    Language language2 = new Language();
    language2.setName("L2");
    language2.setID("l2");
    Language language3 = new Language();
    language3.setName("L3");
    language3.setID("l3");

    language.addDependency(language2);
    language.addDependency(language3);
    Reference languageDependsOn = LionCore.getLanguage().getReferenceByName("dependsOn");
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceAddedRecord(
                language, languageDependsOn, 0, new ReferenceValue(language2, "L2")),
            new MockPartitionObserver.ReferenceAddedRecord(
                language, languageDependsOn, 1, new ReferenceValue(language3, "L3"))),
        observer.getRecords());
    observer.clearRecords();

    // referenceValueChanged
    language.setResolveInfo(languageDependsOn, 0, "Language 2");
    language.setReferred(languageDependsOn, 0, new ProxyNode("12345"));
    language.setReferred(languageDependsOn, 1, new ProxyNode("23456"));
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceChangedRecord(
                language, languageDependsOn, 0, "l2", "L2", "l2", "Language 2"),
            new MockPartitionObserver.ReferenceChangedRecord(
                language, languageDependsOn, 0, "l2", "Language 2", "12345", "Language 2"),
            new MockPartitionObserver.ReferenceChangedRecord(
                language, languageDependsOn, 1, "l3", "L3", "23456", "L3")),
        observer.getRecords());
    observer.clearRecords();

    // referenceValueRemoved
    language.removeReferenceValue(languageDependsOn, 1);
    language.removeReferenceValue(languageDependsOn, 0);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceRemovedRecord(
                language, languageDependsOn, 1, "23456", "L3"),
            new MockPartitionObserver.ReferenceRemovedRecord(
                language, languageDependsOn, 0, "12345", "Language 2")),
        observer.getRecords());
    observer.clearRecords();
  }

  @Test
  public void getAndSetPropertyValueWithInvalidProperty() {
    Language language = new Language();
    Property abs = LionCore.getConcept().requirePropertyByName("abstract");
    assertThrows(IllegalArgumentException.class, () -> language.getPropertyValue(abs));
    assertThrows(IllegalArgumentException.class, () -> language.setPropertyValue(abs, false));
  }

  @Test
  public void getChildrenWithInvalidContainment() {
    Language language = new Language();
    Containment fields = LionCore.getStructuredDataType().requireContainmentByName("fields");
    assertThrows(IllegalArgumentException.class, () -> language.getChildren(fields));
    Field field = new Field();
    assertThrows(IllegalArgumentException.class, () -> language.addChild(fields, field));
  }

  @Test
  public void testAddReferenceMultipleValueWithIndex() {
    Language language = new Language();
    language.setID("l1");
    MockPartitionObserver observer = new MockPartitionObserver();
    language.registerPartitionObserver(observer);

    Language dep1 = new Language();
    dep1.setName("Dep1");
    dep1.setID("dep1");

    Language dep2 = new Language();
    dep2.setName("Dep2");
    dep2.setID("dep2");

    ReferenceValue refValue1 = new ReferenceValue(dep1, "Dep1");
    ReferenceValue refValue2 = new ReferenceValue(dep2, "Dep2");

    // Test adding at index 0 to empty list
    int result1 = language.addReferenceMultipleValue("dependsOn", 0, refValue1);
    assertEquals(0, result1);

    // Test adding at index 1
    int result2 = language.addReferenceMultipleValue("dependsOn", 1, refValue2);
    assertEquals(1, result2);

    Reference dependsOn = LionCore.getLanguage().getReferenceByName("dependsOn");
    List<ReferenceValue> refs = language.getReferenceValues(dependsOn);
    assertEquals(2, refs.size());
    assertEquals(refValue1, refs.get(0));
    assertEquals(refValue2, refs.get(1));

    observer.clearRecords();
  }

  @Test
  public void testAddReferenceMultipleValueWithInvalidIndex() {
    Language language = new Language();
    language.setID("l1");

    Language dep = new Language();
    dep.setName("Dep");
    dep.setID("dep");
    ReferenceValue refValue = new ReferenceValue(dep, "Dep");

    // Test negative index
    assertThrows(
        IllegalArgumentException.class,
        () -> language.addReferenceMultipleValue("dependsOn", -1, refValue));

    // Test index greater than size
    assertThrows(
        IllegalArgumentException.class,
        () -> language.addReferenceMultipleValue("dependsOn", 1, refValue));
  }

  @Test
  public void testAddReferenceMultipleValueWithNullValue() {
    Language language = new Language();
    language.setID("l1");

    // Test adding null value
    int result = language.addReferenceMultipleValue("dependsOn", null);
    assertEquals(-1, result);

    // Test adding null value with index
    int resultWithIndex = language.addReferenceMultipleValue("dependsOn", 0, null);
    assertEquals(-1, resultWithIndex);
  }

  @Test
  public void testAddContainmentMultipleValueWithIndex() {
    Language language = new Language();
    language.setID("l1");
    MockPartitionObserver observer = new MockPartitionObserver();
    language.registerPartitionObserver(observer);

    Concept c1 = new Concept();
    c1.setID("c1");
    Concept c2 = new Concept();
    c2.setID("c2");
    Concept c3 = new Concept();
    c3.setID("c3");

    // Add first element at index 0
    boolean result1 = language.addContainmentMultipleValue("entities", c1, 0);
    assertTrue(result1);
    assertEquals(language, c1.getParent());

    // Add second element at index 1
    boolean result2 = language.addContainmentMultipleValue("entities", c2, 1);
    assertTrue(result2);

    // Insert third element at index 1 (between c1 and c2)
    boolean result3 = language.addContainmentMultipleValue("entities", c3, 1);
    assertTrue(result3);

    Containment entities = LionCore.getLanguage().getContainmentByName("entities");
    List<Node> children = language.getChildren(entities);
    assertEquals(3, children.size());
    assertEquals(c1, children.get(0));
    assertEquals(c3, children.get(1)); // c3 was inserted at index 1
    assertEquals(c2, children.get(2)); // c2 was moved to index 2

    // Verify observer notifications
    assertEquals(3, observer.getRecords().size());
    assertTrue(observer.getRecords().get(0) instanceof MockPartitionObserver.ChildAddedRecord);
    assertTrue(observer.getRecords().get(1) instanceof MockPartitionObserver.ChildAddedRecord);
    assertTrue(observer.getRecords().get(2) instanceof MockPartitionObserver.ChildAddedRecord);

    observer.clearRecords();
  }

  @Test
  public void testAddContainmentMultipleValueWithNullValue() {
    Language language = new Language();
    language.setID("l1");

    // Test adding null value
    boolean result1 = language.addContainmentMultipleValue("entities", null);
    assertFalse(result1);

    // Test adding null value with index
    boolean result2 = language.addContainmentMultipleValue("entities", null, 0);
    assertFalse(result2);
  }

  @Test
  public void testAddContainmentMultipleValueWithDuplicateValue() {
    Language language = new Language();
    language.setID("l1");

    Concept concept = new Concept();
    concept.setID("c1");

    // Add the concept first time
    boolean result1 = language.addContainmentMultipleValue("entities", concept);
    assertTrue(result1);

    // Try to add the same concept again
    boolean result2 = language.addContainmentMultipleValue("entities", concept);
    assertFalse(result2);

    // Try to add the same concept with index
    boolean result3 = language.addContainmentMultipleValue("entities", concept, 0);
    assertFalse(result3);

    Containment entities = LionCore.getLanguage().getContainmentByName("entities");
    List<Node> children = language.getChildren(entities);
    assertEquals(1, children.size()); // Should still be only one
  }

  @Test
  public void testSetReferenceValues() {
    Language language = new Language();
    language.setID("l1");
    MockPartitionObserver observer = new MockPartitionObserver();
    language.registerPartitionObserver(observer);

    Language dep1 = new Language();
    dep1.setName("Dep1");
    dep1.setID("dep1");

    Language dep2 = new Language();
    dep2.setName("Dep2");
    dep2.setID("dep2");

    Language dep3 = new Language();
    dep3.setName("Dep3");
    dep3.setID("dep3");

    // First add some dependencies
    language.addDependency(dep1);
    language.addDependency(dep2);

    Reference dependsOn = LionCore.getLanguage().getReferenceByName("dependsOn");
    assertEquals(2, language.getReferenceValues(dependsOn).size());

    observer.clearRecords();

    // Now replace all reference values with a new list
    List<ReferenceValue> newValues = Arrays.asList(new ReferenceValue(dep3, "Dep3"));

    language.setReferenceValues(dependsOn, newValues);

    List<ReferenceValue> refs = language.getReferenceValues(dependsOn);
    assertEquals(1, refs.size());
    assertEquals("dep3", refs.get(0).getReferredID());
    assertEquals("Dep3", refs.get(0).getResolveInfo());

    // Verify observer was notified of removals and additions
    List<MockPartitionObserver.Record> records = observer.getRecords();
    assertTrue(records.size() >= 3); // 2 removals + 1 addition

    observer.clearRecords();
  }

  @Test
  public void testAddReferenceValueWithReference() {
    Language language = new Language();
    language.setID("l1");
    MockPartitionObserver observer = new MockPartitionObserver();
    language.registerPartitionObserver(observer);

    Language dep = new Language();
    dep.setName("Dep");
    dep.setID("dep");

    Reference dependsOn = LionCore.getLanguage().getReferenceByName("dependsOn");
    ReferenceValue refValue = new ReferenceValue(dep, "Dep");

    // Test addReferenceValue with Reference parameter
    int result = language.addReferenceValue(dependsOn, refValue);
    assertEquals(0, result);

    List<ReferenceValue> refs = language.getReferenceValues(dependsOn);
    assertEquals(1, refs.size());
    assertEquals(refValue, refs.get(0));

    observer.clearRecords();
  }

  @Test
  public void testAddReferenceValueWithReferenceAndIndex() {
    Language language = new Language();
    language.setID("l1");

    Language dep1 = new Language();
    dep1.setName("Dep1");
    dep1.setID("dep1");

    Language dep2 = new Language();
    dep2.setName("Dep2");
    dep2.setID("dep2");

    Reference dependsOn = LionCore.getLanguage().getReferenceByName("dependsOn");
    ReferenceValue refValue1 = new ReferenceValue(dep1, "Dep1");
    ReferenceValue refValue2 = new ReferenceValue(dep2, "Dep2");

    // Add first reference
    language.addReferenceValue(dependsOn, refValue1);

    // Add second reference at index 0 (should insert at beginning)
    int result = language.addReferenceValue(dependsOn, 0, refValue2);
    assertEquals(1, result);

    List<ReferenceValue> refs = language.getReferenceValues(dependsOn);
    assertEquals(2, refs.size());
    assertEquals(refValue2, refs.get(0)); // refValue2 was inserted at index 0
    assertEquals(refValue1, refs.get(1)); // refValue1 was moved to index 1
  }

  @Test
  public void testAddChildWithIndex() {
    Language language = new Language();
    language.setID("l1");

    Concept c1 = new Concept();
    c1.setID("c1");
    Concept c2 = new Concept();
    c2.setID("c2");

    Containment entities = LionCore.getLanguage().getContainmentByName("entities");

    // Add first child at index 0
    language.addChild(entities, c1, 0);

    // Add second child at index 0 (should insert at beginning)
    language.addChild(entities, c2, 0);

    List<Node> children = language.getChildren(entities);
    assertEquals(2, children.size());
    assertEquals(c2, children.get(0)); // c2 was inserted at index 0
    assertEquals(c1, children.get(1)); // c1 was moved to index 1
  }

  @Test
  public void testAddChildWithNegativeIndex() {
    Language language = new Language();
    language.setID("l1");

    Concept concept = new Concept();
    concept.setID("c1");

    Containment entities = LionCore.getLanguage().getContainmentByName("entities");

    assertThrows(IllegalArgumentException.class, () -> language.addChild(entities, concept, -1));
  }

  @Test
  public void testSetReferenceSingleValue() {
    // Create a concept with a single reference for testing
    Concept concept = new Concept();
    concept.setID("c1");
    MockPartitionObserver observer = new MockPartitionObserver();
    concept.registerPartitionObserver(observer);

    Concept extendedConcept = new Concept();
    extendedConcept.setID("extended");
    extendedConcept.setName("ExtendedConcept");

    ReferenceValue refValue = new ReferenceValue(extendedConcept, "ExtendedConcept");

    // Set single reference value using the protected method indirectly
    concept.setExtendedConcept(extendedConcept);

    assertEquals(extendedConcept, concept.getExtendedConcept());

    // Verify observer was notified
    assertTrue(observer.getRecords().size() > 0);
    assertTrue(observer.getRecords().get(0) instanceof MockPartitionObserver.ReferenceAddedRecord);

    observer.clearRecords();

    // Set to null
    concept.setExtendedConcept(null);
    assertNull(concept.getExtendedConcept());

    observer.clearRecords();
  }
}
