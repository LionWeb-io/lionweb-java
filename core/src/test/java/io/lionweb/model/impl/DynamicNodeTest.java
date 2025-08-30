package io.lionweb.model.impl;

import static org.junit.Assert.*;

import com.google.gson.JsonArray;
import io.lionweb.language.*;
import io.lionweb.model.*;
import io.lionweb.serialization.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class DynamicNodeTest {

  @Test
  public void equalityPositiveCaseEmptyNodes() {
    MyNodeWithProperties n1 = new MyNodeWithProperties("id1");
    MyNodeWithProperties n2 = new MyNodeWithProperties("id1");
    assertEquals(n1, n2);
  }

  @Test
  public void equalityNegativeCaseEmptyNodes() {
    MyNodeWithProperties n1 = new MyNodeWithProperties("id1");
    MyNodeWithProperties n2 = new MyNodeWithProperties("id2");
    assertNotEquals(n1, n2);
  }

  @Test
  public void equalityPositiveCaseWithProperties() {
    MyNodeWithProperties2023 n1 = new MyNodeWithProperties2023("id1");
    n1.setP1(true);
    n1.setP2(123);
    n1.setP3("foo");
    n1.setP4(new JsonArray());
    MyNodeWithProperties2023 n2 = new MyNodeWithProperties2023("id1");
    n2.setP1(true);
    n2.setP2(123);
    n2.setP3("foo");
    n2.setP4(new JsonArray());
    assertEquals(n1, n2);
  }

  @Test
  public void equalityNegativeCaseWithProperties() {
    MyNodeWithProperties2023 n1 = new MyNodeWithProperties2023("id1");
    n1.setP1(true);
    n1.setP2(123);
    n1.setP3("foo");
    n1.setP4(new JsonArray());
    MyNodeWithProperties2023 n2 = new MyNodeWithProperties2023("id1");
    n2.setP1(true);
    n2.setP2(123);
    n2.setP3("bar");
    n2.setP4(new JsonArray());
    assertNotEquals(n1, n2);
  }

  @Test
  public void removeChildOnSingleContainment() {
    Concept c = new Concept();
    Containment containment = Containment.createOptional("ch", c);
    containment.setKey("my-containment");
    c.addFeature(containment);
    DynamicNode n1 = new DynamicNode("id-123", c);
    DynamicNode n2 = new DynamicNode("id-456", c);

    assertEquals(Arrays.asList(), n1.getChildren(containment));
    n1.addChild(containment, n2);
    assertEquals(Arrays.asList(n2), n1.getChildren(containment));
    n1.removeChild(n2);
    assertEquals(Arrays.asList(), n1.getChildren(containment));
  }

  @Test
  public void removeChildOnMultipleContainment() {
    Concept c = new Concept();
    Containment containment = Containment.createMultiple("ch", c);
    containment.setKey("my-containment");
    c.addFeature(containment);
    DynamicNode n1 = new DynamicNode("id-123", c);
    DynamicNode n2 = new DynamicNode("id-456", c);
    DynamicNode n3 = new DynamicNode("id-789", c);
    DynamicNode n4 = new DynamicNode("id-012", c);

    assertEquals(Arrays.asList(), n1.getChildren(containment));
    n1.addChild(containment, n2);
    n1.addChild(containment, n3);
    n1.addChild(containment, n4);
    assertEquals(Arrays.asList(n2, n3, n4), n1.getChildren(containment));
    n1.removeChild(n3);
    assertEquals(Arrays.asList(n2, n4), n1.getChildren(containment));
    n1.removeChild(n2);
    assertEquals(Arrays.asList(n4), n1.getChildren(containment));
    n1.removeChild(n4);
    assertEquals(Arrays.asList(), n1.getChildren(containment));
  }

  @Test
  public void removeChildOnMultipleContainmentByIndex() {
    Concept c = new Concept();
    Containment containment = Containment.createMultiple("ch", c);
    containment.setKey("my-containment");
    c.addFeature(containment);
    DynamicNode n1 = new DynamicNode("id-123", c);
    DynamicNode n2 = new DynamicNode("id-456", c);
    DynamicNode n3 = new DynamicNode("id-789", c);
    DynamicNode n4 = new DynamicNode("id-012", c);

    assertEquals(Arrays.asList(), n1.getChildren(containment));
    n1.addChild(containment, n2);
    n1.addChild(containment, n3);
    n1.addChild(containment, n4);
    assertEquals(Arrays.asList(n2, n3, n4), n1.getChildren(containment));
    n1.removeChild(containment, 1);
    assertEquals(Arrays.asList(n2, n4), n1.getChildren(containment));
    n1.removeChild(containment, 0);
    assertEquals(Arrays.asList(n4), n1.getChildren(containment));
    n1.removeChild(containment, 0);
    assertEquals(Arrays.asList(), n1.getChildren(containment));
  }

  @Test
  public void addAnnotations() {
    Language l = new Language("l");
    Annotation a1 = new Annotation(l, "a1");
    Annotation a2 = new Annotation(l, "a2");
    Concept c = new Concept(l, "c");

    DynamicNode n1 = new DynamicNode("n1", c);
    assertEquals(Arrays.asList(), n1.getAnnotations());
    assertEquals(Arrays.asList(), n1.getAnnotations(a1));
    assertEquals(Arrays.asList(), n1.getAnnotations(a2));

    AnnotationInstance a1_1 = new DynamicAnnotationInstance("a1_1", a1, n1);
    assertEquals(Arrays.asList(a1_1), n1.getAnnotations());
    assertEquals(Arrays.asList(a1_1), n1.getAnnotations(a1));
    assertEquals(Arrays.asList(), n1.getAnnotations(a2));

    AnnotationInstance a1_2 = new DynamicAnnotationInstance("a1_2", a1, n1);
    assertEquals(Arrays.asList(a1_1, a1_2), n1.getAnnotations());
    assertEquals(Arrays.asList(a1_1, a1_2), n1.getAnnotations(a1));
    assertEquals(Arrays.asList(), n1.getAnnotations(a2));

    AnnotationInstance a2_3 = new DynamicAnnotationInstance("a2_3", a2, n1);
    assertEquals(Arrays.asList(a1_1, a1_2, a2_3), n1.getAnnotations());
    assertEquals(Arrays.asList(a1_1, a1_2), n1.getAnnotations(a1));
    assertEquals(Arrays.asList(a2_3), n1.getAnnotations(a2));

    AnnotationInstance a2_4 = new DynamicAnnotationInstance("a2_4", a2, n1);
    assertEquals(Arrays.asList(a1_1, a1_2, a2_3, a2_4), n1.getAnnotations());
    assertEquals(Arrays.asList(a1_1, a1_2), n1.getAnnotations(a1));
    assertEquals(Arrays.asList(a2_3, a2_4), n1.getAnnotations(a2));

    n1.removeAnnotation(a2_3);
    assertNull(a2_3.getParent());
    assertEquals(Arrays.asList(a1_1, a1_2, a2_4), n1.getAnnotations());
    assertEquals(Arrays.asList(a1_1, a1_2), n1.getAnnotations(a1));
    assertEquals(Arrays.asList(a2_4), n1.getAnnotations(a2));
  }

  @Test
  public void getRootSimpleCases() {
    Language l = new Language("MyLanguage", "l-id", "l-key", "123");
    Concept a = new Concept(l, "A", "a-id", "a-key");
    DynamicNode n1 = new DynamicNode("n1", a);
    DynamicNode n2 = new DynamicNode("n2", a);
    DynamicNode n3 = new DynamicNode("n3", a);
    DynamicNode n4 = new DynamicNode("n4", a);

    n2.setParent(n1);
    n3.setParent(n2);
    n4.setParent(n3);

    assertEquals(n1, n1.getRoot());
    assertEquals(n1, n2.getRoot());
    assertEquals(n1, n3.getRoot());
    assertEquals(n1, n4.getRoot());
  }

  @Test
  public void getRootCircularHierarchy() {
    Language l = new Language("MyLanguage", "l-id", "l-key", "123");
    Concept a = new Concept(l, "A", "a-id", "a-key");
    DynamicNode n1 = new DynamicNode("n1", a);
    DynamicNode n2 = new DynamicNode("n2", a);
    DynamicNode n3 = new DynamicNode("n3", a);
    DynamicNode n4 = new DynamicNode("n4", a);

    n1.setParent(n4);
    n2.setParent(n1);
    n3.setParent(n2);
    n4.setParent(n3);

    assertThrows(IllegalStateException.class, () -> n1.getRoot());
    assertThrows(IllegalStateException.class, () -> n2.getRoot());
    assertThrows(IllegalStateException.class, () -> n3.getRoot());
    assertThrows(IllegalStateException.class, () -> n4.getRoot());
  }

  @Test
  public void settingFalseNonNullableBooleanProperty() {
    Language l = new Language("MyLanguage", "l-id", "l-key", "123");
    Concept a = new Concept(l, "A", "a-id", "a-key");
    a.addFeature(
        Property.createRequired("foo", LionCoreBuiltins.getBoolean())
            .setID("foo-id")
            .setKey("foo-key"));
    DynamicNode n1 = new DynamicNode("n1", a);

    assertEquals(false, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", false);
    assertEquals(false, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
  }

  @Test
  public void settingTrueNonNullableBooleanProperty() {
    Language l = new Language("MyLanguage", "l-id", "l-key", "123");
    Concept a = new Concept(l, "A", "a-id", "a-key");
    a.addFeature(
        Property.createRequired("foo", LionCoreBuiltins.getBoolean())
            .setID("foo-id")
            .setKey("foo-key"));
    DynamicNode n1 = new DynamicNode("n1", a);

    assertEquals(false, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", true);
    assertEquals(true, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
  }

  @Test
  public void settingNullNonNullableBooleanProperty() {
    Language l = new Language("MyLanguage", "l-id", "l-key", "123");
    Concept a = new Concept(l, "A", "a-id", "a-key");
    a.addFeature(
        Property.createRequired("foo", LionCoreBuiltins.getBoolean())
            .setID("foo-id")
            .setKey("foo-key"));
    DynamicNode n1 = new DynamicNode("n1", a);

    assertEquals(false, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
    // This is interpreted as "go back to default value"
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", null);
    assertEquals(false, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
  }

  @Test
  public void settingFalseNullableBooleanProperty() {
    Language l = new Language("MyLanguage", "l-id", "l-key", "123");
    Concept a = new Concept(l, "A", "a-id", "a-key");
    a.addFeature(
        Property.createOptional("foo", LionCoreBuiltins.getBoolean())
            .setID("foo-id")
            .setKey("foo-key"));
    DynamicNode n1 = new DynamicNode("n1", a);

    assertEquals(null, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", false);
    assertEquals(false, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
  }

  @Test
  public void settingNullNullableBooleanProperty() {
    Language l = new Language("MyLanguage", "l-id", "l-key", "123");
    Concept a = new Concept(l, "A", "a-id", "a-key");
    a.addFeature(
        Property.createOptional("foo", LionCoreBuiltins.getBoolean())
            .setID("foo-id")
            .setKey("foo-key"));
    DynamicNode n1 = new DynamicNode("n1", a);

    assertEquals(null, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", null);
    assertEquals(null, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));

    // Check also what happens when we null a value that was previously not null
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", true);
    assertEquals(true, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));

    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", null);
    assertEquals(null, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
  }

  @Test
  public void settingTrueNullableBooleanProperty() {
    Language l = new Language("MyLanguage", "l-id", "l-key", "123");
    Concept a = new Concept(l, "A", "a-id", "a-key");
    a.addFeature(
        Property.createOptional("foo", LionCoreBuiltins.getBoolean())
            .setID("foo-id")
            .setKey("foo-key"));
    DynamicNode n1 = new DynamicNode("n1", a);

    assertEquals(null, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", true);
    assertEquals(true, ClassifierInstanceUtils.getPropertyValueByName(n1, "foo"));
  }

  @Test
  public void getReferenceValuesWithoutParameter() {
    DynamicNode n1 = new DynamicNode("n1", MyNodeWithReferences.CONCEPT);

    Reference r1 = n1.getClassifier().getReferenceByName("r1");
    Reference r2 = n1.getClassifier().getReferenceByName("r2");
    assertEquals(Collections.emptyList(), ClassifierInstanceUtils.getReferenceValues(n1));

    n1.addReferenceValue(r2, new ReferenceValue(null, "bar"));
    assertEquals(
        Arrays.asList(new ReferenceValue(null, "bar")),
        ClassifierInstanceUtils.getReferenceValues(n1));

    n1.addReferenceValue(r1, new ReferenceValue(null, "foo"));
    assertEquals(
        Arrays.asList(new ReferenceValue(null, "foo"), new ReferenceValue(null, "bar")),
        ClassifierInstanceUtils.getReferenceValues(n1));

    n1.addReferenceValue(r2, new ReferenceValue(null, "baz"));
    assertEquals(
        Arrays.asList(
            new ReferenceValue(null, "foo"),
            new ReferenceValue(null, "bar"),
            new ReferenceValue(null, "baz")),
        ClassifierInstanceUtils.getReferenceValues(n1));
  }

  @Test
  public void getReferenceValuesWithParameter() {
    DynamicNode n1 = new DynamicNode("n1", MyNodeWithReferences.CONCEPT);

    Reference r1 = n1.getClassifier().getReferenceByName("r1");
    Reference r2 = n1.getClassifier().getReferenceByName("r2");
    assertEquals(Collections.emptyList(), ClassifierInstanceUtils.getReferenceValues(n1));

    n1.addReferenceValue(r2, new ReferenceValue(null, "bar"));
    assertEquals(Arrays.asList(), n1.getReferenceValues(r1));
    assertEquals(Arrays.asList(new ReferenceValue(null, "bar")), n1.getReferenceValues(r2));

    n1.addReferenceValue(r1, new ReferenceValue(null, "foo"));
    assertEquals(Arrays.asList(new ReferenceValue(null, "foo")), n1.getReferenceValues(r1));
    assertEquals(Arrays.asList(new ReferenceValue(null, "bar")), n1.getReferenceValues(r2));

    n1.addReferenceValue(r2, new ReferenceValue(null, "baz"));
    assertEquals(Arrays.asList(new ReferenceValue(null, "foo")), n1.getReferenceValues(r1));
    assertEquals(
        Arrays.asList(new ReferenceValue(null, "bar"), new ReferenceValue(null, "baz")),
        n1.getReferenceValues(r2));
  }

  @Test
  public void getReferredNodesWithoutParameter() {
    DynamicNode n1 = new DynamicNode("n1", MyNodeWithReferences.CONCEPT);
    DynamicNode n2 = new DynamicNode("n2", MyNodeWithReferences.CONCEPT);

    Reference r1 = n1.getClassifier().getReferenceByName("r1");
    Reference r2 = n1.getClassifier().getReferenceByName("r2");
    assertEquals(Collections.emptyList(), ClassifierInstanceUtils.getReferenceValues(n1));

    n1.addReferenceValue(r2, new ReferenceValue(n1, "bar"));
    assertEquals(Arrays.asList(n1), ClassifierInstanceUtils.getReferredNodes(n1));

    n1.addReferenceValue(r1, new ReferenceValue(n2, "foo"));
    assertEquals(Arrays.asList(n2, n1), ClassifierInstanceUtils.getReferredNodes(n1));

    n1.addReferenceValue(r2, new ReferenceValue(n2, "baz"));
    assertEquals(Arrays.asList(n2, n1, n2), ClassifierInstanceUtils.getReferredNodes(n1));

    n1.addReferenceValue(r2, new ReferenceValue(null, "baz3"));
    assertEquals(Arrays.asList(n2, n1, n2), ClassifierInstanceUtils.getReferredNodes(n1));
  }

  @Test
  public void getReferredNodesWithParameter() {
    DynamicNode n1 = new DynamicNode("n1", MyNodeWithReferences.CONCEPT);
    DynamicNode n2 = new DynamicNode("n2", MyNodeWithReferences.CONCEPT);

    Reference r1 = n1.getClassifier().getReferenceByName("r1");
    Reference r2 = n1.getClassifier().getReferenceByName("r2");
    assertEquals(Collections.emptyList(), ClassifierInstanceUtils.getReferenceValues(n1));

    n1.addReferenceValue(r2, new ReferenceValue(n1, "bar"));
    assertEquals(Arrays.asList(), ClassifierInstanceUtils.getReferredNodes(n1, r1));
    assertEquals(Arrays.asList(n1), ClassifierInstanceUtils.getReferredNodes(n1, r2));

    n1.addReferenceValue(r1, new ReferenceValue(n2, "foo"));
    assertEquals(Arrays.asList(n2), ClassifierInstanceUtils.getReferredNodes(n1, r1));
    assertEquals(Arrays.asList(n1), ClassifierInstanceUtils.getReferredNodes(n1, r2));

    n1.addReferenceValue(r2, new ReferenceValue(n2, "baz"));
    assertEquals(Arrays.asList(n2), ClassifierInstanceUtils.getReferredNodes(n1, r1));
    assertEquals(Arrays.asList(n1, n2), ClassifierInstanceUtils.getReferredNodes(n1, r2));

    n1.addReferenceValue(r2, new ReferenceValue(null, "baz3"));
    assertEquals(Arrays.asList(n2), ClassifierInstanceUtils.getReferredNodes(n1, r1));
    assertEquals(Arrays.asList(n1, n2, null), ClassifierInstanceUtils.getReferredNodes(n1, r2));
  }

  @Test
  public void removeReferenceValueByValue() {
    DynamicNode n1 = new DynamicNode("n1", MyNodeWithReferences.CONCEPT);
    DynamicNode n2 = new DynamicNode("n2", MyNodeWithReferences.CONCEPT);

    Reference r1 = n1.getClassifier().getReferenceByName("r1");
    Reference r2 = n1.getClassifier().getReferenceByName("r2");
    assertEquals(Collections.emptyList(), ClassifierInstanceUtils.getReferenceValues(n1));

    n1.addReferenceValue(r1, new ReferenceValue(n2, "foo"));
    n1.addReferenceValue(r2, new ReferenceValue(n1, "bar"));
    n1.addReferenceValue(r2, new ReferenceValue(n2, "baz"));
    n1.addReferenceValue(r2, new ReferenceValue(null, "baz3"));
    assertEquals(Arrays.asList(new ReferenceValue(n2, "foo")), n1.getReferenceValues(r1));
    assertEquals(
        Arrays.asList(
            new ReferenceValue(n1, "bar"),
            new ReferenceValue(n2, "baz"),
            new ReferenceValue(null, "baz3")),
        n1.getReferenceValues(r2));

    n1.removeReferenceValue(r2, new ReferenceValue(n1, "bar"));
    assertEquals(Arrays.asList(new ReferenceValue(n2, "foo")), n1.getReferenceValues(r1));
    assertEquals(
        Arrays.asList(new ReferenceValue(n2, "baz"), new ReferenceValue(null, "baz3")),
        n1.getReferenceValues(r2));

    n1.removeReferenceValue(r2, new ReferenceValue(null, "baz3"));
    assertEquals(Arrays.asList(new ReferenceValue(n2, "foo")), n1.getReferenceValues(r1));
    assertEquals(Arrays.asList(new ReferenceValue(n2, "baz")), n1.getReferenceValues(r2));

    n1.removeReferenceValue(r1, new ReferenceValue(n2, "foo"));
    assertEquals(Arrays.asList(), n1.getReferenceValues(r1));
    assertEquals(Arrays.asList(new ReferenceValue(n2, "baz")), n1.getReferenceValues(r2));

    n1.removeReferenceValue(r2, new ReferenceValue(n2, "baz"));
    assertEquals(Arrays.asList(), n1.getReferenceValues(r1));
    assertEquals(Arrays.asList(), n1.getReferenceValues(r2));
  }

  @Test
  public void removeReferenceValueByIndex() {
    DynamicNode n1 = new DynamicNode("n1", MyNodeWithReferences.CONCEPT);
    DynamicNode n2 = new DynamicNode("n2", MyNodeWithReferences.CONCEPT);

    Reference r1 = n1.getClassifier().getReferenceByName("r1");
    Reference r2 = n1.getClassifier().getReferenceByName("r2");
    assertEquals(Collections.emptyList(), ClassifierInstanceUtils.getReferenceValues(n1));

    n1.addReferenceValue(r1, new ReferenceValue(n2, "foo"));
    n1.addReferenceValue(r2, new ReferenceValue(n1, "bar"));
    n1.addReferenceValue(r2, new ReferenceValue(n2, "baz"));
    n1.addReferenceValue(r2, new ReferenceValue(null, "baz3"));
    assertEquals(Arrays.asList(new ReferenceValue(n2, "foo")), n1.getReferenceValues(r1));
    assertEquals(
        Arrays.asList(
            new ReferenceValue(n1, "bar"),
            new ReferenceValue(n2, "baz"),
            new ReferenceValue(null, "baz3")),
        n1.getReferenceValues(r2));

    n1.removeReferenceValue(r2, 0);
    assertEquals(Arrays.asList(new ReferenceValue(n2, "foo")), n1.getReferenceValues(r1));
    assertEquals(
        Arrays.asList(new ReferenceValue(n2, "baz"), new ReferenceValue(null, "baz3")),
        n1.getReferenceValues(r2));

    n1.removeReferenceValue(r2, 1);
    assertEquals(Arrays.asList(new ReferenceValue(n2, "foo")), n1.getReferenceValues(r1));
    assertEquals(Arrays.asList(new ReferenceValue(n2, "baz")), n1.getReferenceValues(r2));

    n1.removeReferenceValue(r1, 0);
    assertEquals(Arrays.asList(), n1.getReferenceValues(r1));
    assertEquals(Arrays.asList(new ReferenceValue(n2, "baz")), n1.getReferenceValues(r2));

    n1.removeReferenceValue(r2, 0);
    assertEquals(Arrays.asList(), n1.getReferenceValues(r1));
    assertEquals(Arrays.asList(), n1.getReferenceValues(r2));
  }

  @Test
  public void testGetContainmentFeature() {
    MyNodeWithSelfContainment n1 = new MyNodeWithSelfContainment("n1");
    MyNodeWithSelfContainment n2 = new MyNodeWithSelfContainment("n1");
    MyNodeWithSelfContainment n3 = new MyNodeWithSelfContainment("n1");
    n1.setAnother(n2);
    n2.setAnother(n3);
    assertEquals(null, n1.getContainmentFeature());
    assertEquals("another", n2.getContainmentFeature().getName());
    assertEquals("another-id", n2.getContainmentFeature().getID());
    assertEquals("another-key", n2.getContainmentFeature().getKey());
    assertEquals("another", n3.getContainmentFeature().getName());
    assertEquals("another-id", n3.getContainmentFeature().getID());
    assertEquals("another-key", n3.getContainmentFeature().getKey());
  }

  @Test
  public void nodeWithStructuredDataType() {
    DynamicStructuredDataTypeInstance point1 =
        new DynamicStructuredDataTypeInstance(MyNodeWithStructuredDataType.POINT);
    StructuredDataTypeInstanceUtils.setFieldValueByName(point1, "x", 10);
    StructuredDataTypeInstanceUtils.setFieldValueByName(point1, "y", 14);

    MyNodeWithStructuredDataType n1 = new MyNodeWithStructuredDataType("n1");
    n1.setPoint(point1);
    assertEquals(point1, n1.getPoint());
  }

  @Test
  public void nodeWithAmount() {
    DynamicStructuredDataTypeInstance value =
        new DynamicStructuredDataTypeInstance(MyNodeWithAmount.DECIMAL);
    StructuredDataTypeInstanceUtils.setFieldValueByName(value, "int", 2);
    StructuredDataTypeInstanceUtils.setFieldValueByName(value, "frac", 3);

    DynamicStructuredDataTypeInstance amount =
        new DynamicStructuredDataTypeInstance(MyNodeWithAmount.AMOUNT);
    StructuredDataTypeInstanceUtils.setFieldValueByName(amount, "value", value);
    EnumerationLiteral euro = MyNodeWithAmount.CURRENCY.getLiterals().get(0);
    StructuredDataTypeInstanceUtils.setFieldValueByName(
        amount, "currency", new EnumerationValueImpl(euro));
    assertEquals(
        new EnumerationValueImpl(euro),
        StructuredDataTypeInstanceUtils.getFieldValueByName(amount, "currency"));
    StructuredDataTypeInstanceUtils.setFieldValueByName(amount, "digital", true);

    MyNodeWithAmount n1 = new MyNodeWithAmount("n1");
    n1.setAmount(amount);
    assertEquals(amount, n1.getAmount());
  }

  @Test
  public void equalityConsideringParent() {
    DynamicNode a = new DynamicNode("foo-1", MyNodeWithAmount.CONCEPT);
    DynamicNode b = new DynamicNode("foo-1", MyNodeWithAmount.CONCEPT);
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
    DynamicNode c = new DynamicNode("foo-2", MyNodeWithAmount.CONCEPT);
    a.setParent(c);
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    DynamicNode d = new DynamicNode("foo-2", MyNodeWithAmount.CONCEPT);
    b.setParent(d);
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  @Test
  public void equalityConsideringReferences() {
    DynamicNode node1 = new DynamicNode("id1", MyNodeWithAmount.CONCEPT);
    DynamicNode node2 = new DynamicNode("id1", MyNodeWithAmount.CONCEPT);
    Reference reference = new Reference("ref");

    // Case: both nodes have the same reference values
    node1.addReferenceValue(reference, new ReferenceValue(null, "resolve1"));
    node2.addReferenceValue(reference, new ReferenceValue(null, "resolve1"));
    assertEquals(true, node1.equals(node2));

    // Case: nodes have different reference values
    node2.addReferenceValue(reference, new ReferenceValue(null, "resolve2"));
    assertEquals(false, node1.equals(node2));

    // Case: one node has a null referredID, the other has a non-null referredID
    node1.addReferenceValue(reference, new ReferenceValue(null, "resolve2"));
    node2.addReferenceValue(reference, new ReferenceValue(new DynamicNode(), null));
    assertEquals(false, node1.equals(node2));

    // Case: both nodes have null referredID and same resolveInfo
    node1.addReferenceValue(reference, new ReferenceValue(null, "resolve3"));
    node2.addReferenceValue(reference, new ReferenceValue(null, "resolve3"));
    assertEquals(true, node1.equals(node2));
  }

  @Test
  public void equalityConsideringAnnotations() {
    DynamicNode node1 = new DynamicNode("id1", MyNodeWithAmount.CONCEPT);
    DynamicNode node2 = new DynamicNode("id1", MyNodeWithAmount.CONCEPT);
    Annotation annotation = new Annotation(new Language("lang"), "annotation");

    // Case: both nodes have the same annotations
    node1.addAnnotation(new DynamicAnnotationInstance("a1", annotation, node1));
    node2.addAnnotation(new DynamicAnnotationInstance("a1", annotation, node2));
    assertEquals(true, node1.equals(node2));

    // Case: nodes have different annotations
    node2.addAnnotation(new DynamicAnnotationInstance("a2", annotation, node2));
    assertEquals(false, node1.equals(node2));

    // Case: nodes have the same annotations again
    node1.addAnnotation(new DynamicAnnotationInstance("a2", annotation, node1));
    assertEquals(true, node1.equals(node2));
  }

  @Test
  public void observer() {
    Library library = new Library("library1", "My Glorious Library");

    Book book = new Book("book1");
    library.addBook(book);
    MockPartitionObserver observer = new MockPartitionObserver();
    library.registerPartitionObserver(observer);

    // propertyChanged
    book.setPages(200);
    book.setTitle("La Divina Commedia - 2025");
    Property pages = book.getClassifier().getPropertyByName("pages");
    Property title = book.getClassifier().getPropertyByName("title");
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.PropertyChangedRecord(book, pages, null, 200),
            new MockPartitionObserver.PropertyChangedRecord(
                book, title, null, "La Divina Commedia - 2025")),
        observer.getRecords());
    observer.clearRecords();

    // childAdded
    Book book2 = new Book("book2");
    library.addBook(book2);
    Book book3 = new Book("book3");
    library.addBook(book3);
    Containment books = library.getClassifier().getContainmentByName("books");
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ChildAddedRecord(library, books, 1, book2),
            new MockPartitionObserver.ChildAddedRecord(library, books, 2, book3)),
        observer.getRecords());
    observer.clearRecords();

    // childRemoved
    library.removeChild(book3);
    library.removeChild(book2);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ChildRemovedRecord(library, books, 2, book3),
            new MockPartitionObserver.ChildRemovedRecord(library, books, 1, book2)),
        observer.getRecords());
    observer.clearRecords();

    // annotationAdded
    Annotation annotation = new Annotation();
    annotation.setID("a1");
    AnnotationInstance ann1 = new DynamicAnnotationInstance("ai1", annotation);
    AnnotationInstance ann2 = new DynamicAnnotationInstance("ai2", annotation);
    book.addAnnotation(ann1);
    book.addAnnotation(ann2);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.AnnotationAddedRecord(book, 0, ann1),
            new MockPartitionObserver.AnnotationAddedRecord(book, 1, ann2)),
        observer.getRecords());
    observer.clearRecords();

    // annotationRemoved
    book.removeAnnotation(ann2);
    book.removeAnnotation(ann1);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.AnnotationRemovedRecord(book, 1, ann2),
            new MockPartitionObserver.AnnotationRemovedRecord(book, 0, ann1)),
        observer.getRecords());
    observer.clearRecords();

    // referenceValueAdded and Changed
    Writer writer1 = new Writer("writer1");
    writer1.setName("Dante Alighieri");
    Writer writer2 = new Writer("writer2");
    writer2.setName("Fernando Pessoa");

    book.setAuthor(writer1);
    book.setAuthor(writer2);
    Reference author = book.getClassifier().getReferenceByName("author");
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceAddedRecord(
                book, author, new ReferenceValue(writer1, "Dante Alighieri")),
            new MockPartitionObserver.ReferenceChangedRecord(
                book, author, 0, "writer1", "Dante Alighieri", "writer2", "Fernando Pessoa")),
        observer.getRecords());
    observer.clearRecords();

    // referenceValueRemoved
    book.setAuthor(null);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceRemovedRecord(
                book, author, 0, "writer2", "Fernando Pessoa")),
        observer.getRecords());
    observer.clearRecords();
  }

  @Test
  public void observerPropagationAfterAddingInTree() {
    Library library = new Library("library1", "My Glorious Library");
    MockPartitionObserver observer = new MockPartitionObserver();
    library.registerPartitionObserver(observer);
    Book book = new Book("book1");
    library.addBook(book);

    book.setTitle("La Divina Commedia - 2025");

    Containment books = library.getClassifier().getContainmentByName("books");
    Property title = book.getClassifier().getPropertyByName("title");

    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ChildAddedRecord(library, books, 0, book),
            new MockPartitionObserver.PropertyChangedRecord(
                book, title, null, "La Divina Commedia - 2025")),
        observer.getRecords());
    observer.clearRecords();
  }

  @Test
  public void observerAutoRemovalAfterAddingInTree() {
    Library library = new Library("library1", "My Glorious Library");
    Book book = new Book("book1");
    MockPartitionObserver observer = new MockPartitionObserver();
    book.registerPartitionObserver(observer);

    // When the book is not root anymore, the observer is auto-removed
    library.addBook(book);

    book.setTitle("La Divina Commedia - 2025");

    assertEquals(Collections.emptyList(), observer.getRecords());
    observer.clearRecords();
  }

  @Test
  public void toString_handlesQualifiedNameException() {
    Concept badConcept =
        new Concept() {
          @Override
          public String qualifiedName() {
            throw new RuntimeException("test exception");
          }
        };
    DynamicNode node = new DynamicNode("test-id", badConcept);

    String result = node.toString();
    assertTrue(result.contains("concept=<cannot be calculated>"));
    assertTrue(result.contains("id='test-id'"));
  }

  @Test
  public void registerPartitionObserver_returnFalseWhenAlreadyRegistered() {
    Concept concept = new Concept();
    DynamicNode root = new DynamicNode("root", concept);
    MockPartitionObserver observer = new MockPartitionObserver();

    root.registerPartitionObserver(observer);
    assertFalse(root.registerPartitionObserver(observer));
  }

  @Test
  public void toString_withContainmentsBuildsString() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setMultiple(true);
    containment.setKey("children-key");
    concept.addFeature(containment);

    DynamicNode parent = new DynamicNode("parent", concept);
    DynamicNode child = new DynamicNode("child", concept);

    parent.addChild(containment, child);

    String result = parent.toString();
    assertNotNull(result);
    assertTrue(result.contains("containmentValues={children-key=child}"));
  }

  @Test
  public void registerPartitionObserver_returnsFalseWhenAlreadyRegistered() {
    Concept concept = new Concept();
    DynamicNode root = new DynamicNode("root", concept);
    MockPartitionObserver observer = new MockPartitionObserver();

    // First registration should return true
    assertTrue(root.registerPartitionObserver(observer));

    // Second registration of same observer should return false
    assertFalse(root.registerPartitionObserver(observer));
  }

  @Test
  public void setConcept_allowsChangingConcept() {
    Concept concept1 = new Concept();
    concept1.setName("Concept1");
    Concept concept2 = new Concept();
    concept2.setName("Concept2");

    DynamicNode node = new DynamicNode("test", concept1);
    assertEquals(concept1, node.getClassifier());

    node.setConcept(concept2);
    assertEquals(concept2, node.getClassifier());
  }

  @Test
  public void registerPartitionObserver_throwsOnNonRootNode() {
    Concept concept = new Concept();
    DynamicNode root = new DynamicNode("root", concept);
    DynamicNode child = new DynamicNode("child", concept);
    child.setParent(root);

    MockPartitionObserver observer = new MockPartitionObserver();

    assertFalse(child.isRoot());
    assertThrows(
        UnsupportedOperationException.class, () -> child.registerPartitionObserver(observer));
  }

  @Test
  public void unregisterPartitionObserver_throwsOnNonRootNode() {
    Concept concept = new Concept();
    DynamicNode root = new DynamicNode("root", concept);
    DynamicNode child = new DynamicNode("child", concept);
    child.setParent(root);

    MockPartitionObserver observer = new MockPartitionObserver();

    assertFalse(child.isRoot());
    assertThrows(
        UnsupportedOperationException.class, () -> child.unregisterPartitionObserver(observer));
  }

  @Test
  public void registerPartitionObserver_worksOnRootNode() {
    Concept concept = new Concept();
    DynamicNode root = new DynamicNode("root", concept);
    MockPartitionObserver observer = new MockPartitionObserver();

    assertTrue(root.isRoot());
    assertTrue(root.registerPartitionObserver(observer));
    assertFalse(root.registerPartitionObserver(observer)); // second time returns false
  }

  @Test
  public void unregisterPartitionObserver_createsCompositeWhenMultipleObservers() {
    Concept concept = new Concept();
    DynamicNode root = new DynamicNode("root", concept);
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();
    MockPartitionObserver obs3 = new MockPartitionObserver();

    // Register three observers
    root.registerPartitionObserver(obs1);
    root.registerPartitionObserver(obs2);
    root.registerPartitionObserver(obs3);

    // Remove one - should still have composite with two observers
    root.unregisterPartitionObserver(obs1);
    assertNotNull(root.registeredPartitionObserver());

    // Remove another - should have single observer remaining
    root.unregisterPartitionObserver(obs2);
    assertNotNull(root.registeredPartitionObserver());

    // Remove the last one
    root.unregisterPartitionObserver(obs3);

    // Should not throw - all observers removed successfully
    assertNull(root.registeredPartitionObserver());
  }

  @Test
  public void unregisterPartitionObserver_throwsWhenObserverNotRegistered() {
    Concept concept = new Concept();
    DynamicNode root = new DynamicNode("root", concept);
    MockPartitionObserver registered = new MockPartitionObserver();
    MockPartitionObserver unregistered = new MockPartitionObserver();

    root.registerPartitionObserver(registered);

    // Should throw when trying to unregister an observer that wasn't registered
    assertThrows(
        IllegalArgumentException.class, () -> root.unregisterPartitionObserver(unregistered));
  }

  @Test
  public void compositePartitionObserver_combine_returnsSingleWhenOnlyOne() {
    MockPartitionObserver obs = new MockPartitionObserver();

    // When combining results in only one observer, should return that observer directly
    PartitionObserver result = CompositePartitionObserver.combine(obs, obs);

    // Should return the single observer, not a composite
    assertEquals(obs, result);
  }

  @Test
  public void compositePartitionObserver_getInstance_returnsSingleWhenSetHasOne() {
    MockPartitionObserver obs1 = new MockPartitionObserver();
    MockPartitionObserver obs2 = new MockPartitionObserver();

    CompositePartitionObserver composite =
        (CompositePartitionObserver) CompositePartitionObserver.combine(obs1, obs2);

    // When removing one observer from composite with two, should return single observer
    PartitionObserver result = composite.remove(obs1);

    assertEquals(obs2, result);
    assertFalse(result instanceof CompositePartitionObserver);
  }

  @Test
  public void equals_shallowContainmentsEquality_oneNullOneEmpty() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setKey("children-key");
    containment.setMultiple(true);
    concept.addFeature(containment);

    DynamicNode node1 = new DynamicNode("same-id", concept);
    DynamicNode node2 = new DynamicNode("same-id", concept);

    // node1 has null containmentValues (default)
    // node2 has empty containmentValues - add and remove a child to initialize the map

    DynamicNode tempChild = new DynamicNode("temp", concept);
    node2.addChild(containment, tempChild);
    node2.removeChild(tempChild);

    // One null, one empty - should be equal
    assertEquals(node1, node2);
  }

  @Test
  public void equals_shallowContainmentsEquality_bothNull() {
    Concept concept = new Concept();
    DynamicNode node1 = new DynamicNode("same-id", concept);
    DynamicNode node2 = new DynamicNode("same-id", concept);

    // Both have null containmentValues (default state)
    assertEquals(node1, node2);
  }

  @Test
  public void equals_shallowContainmentsEquality_bothEmpty() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setKey("children-key");
    containment.setMultiple(true);
    concept.addFeature(containment);

    DynamicNode node1 = new DynamicNode("same-id", concept);
    DynamicNode node2 = new DynamicNode("same-id", concept);

    // Initialize both maps as empty by adding and removing children
    DynamicNode temp1 = new DynamicNode("temp1", concept);
    DynamicNode temp2 = new DynamicNode("temp2", concept);

    node1.addChild(containment, temp1);
    node1.removeChild(temp1);

    node2.addChild(containment, temp2);
    node2.removeChild(temp2);

    // Both empty maps - should be equal
    assertEquals(node1, node2);
  }

  @Test
  public void equals_shallowContainmentsEquality_differentChildCount() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setKey("children-key");
    containment.setMultiple(true);
    concept.addFeature(containment);

    DynamicNode node1 = new DynamicNode("same-id", concept);
    DynamicNode node2 = new DynamicNode("same-id", concept);

    DynamicNode child1 = new DynamicNode("child1", concept);
    DynamicNode child2 = new DynamicNode("child2", concept);

    // node1 has 1 child, node2 has 2 children
    node1.addChild(containment, child1);
    node2.addChild(containment, child1);
    node2.addChild(containment, child2);

    assertNotEquals(node1, node2);
  }

  @Test
  public void equals_shallowContainmentsEquality_sameChildrenDifferentOrder() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setKey("children-key");
    containment.setMultiple(true);
    concept.addFeature(containment);

    DynamicNode node1 = new DynamicNode("same-id", concept);
    DynamicNode node2 = new DynamicNode("same-id", concept);

    DynamicNode child1 = new DynamicNode("child1", concept);
    DynamicNode child2 = new DynamicNode("child2", concept);

    // Add same children in different order
    node1.addChild(containment, child1);
    node1.addChild(containment, child2);

    node2.addChild(containment, child2);
    node2.addChild(containment, child1);

    // Should not be equal due to different order (shallowContainmentEquality checks by index)
    assertNotEquals(node1, node2);
  }

  @Test
  public void equals_shallowContainmentsEquality_differentChildIDs() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setKey("children-key");
    containment.setMultiple(true);
    concept.addFeature(containment);

    DynamicNode node1 = new DynamicNode("same-id", concept);
    DynamicNode node2 = new DynamicNode("same-id", concept);

    DynamicNode child1 = new DynamicNode("child1", concept);
    DynamicNode child2 = new DynamicNode("child2", concept);

    // Same position, different child IDs
    node1.addChild(containment, child1);
    node2.addChild(containment, child2);

    assertNotEquals(node1, node2);
  }
}
