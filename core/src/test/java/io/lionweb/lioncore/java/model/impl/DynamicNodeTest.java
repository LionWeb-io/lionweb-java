package io.lionweb.lioncore.java.model.impl;

import static org.junit.Assert.*;

import com.google.gson.JsonArray;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.model.StructuredDataTypeInstanceUtils;
import io.lionweb.lioncore.java.serialization.*;
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
}
