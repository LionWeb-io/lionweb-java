package io.lionweb.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import io.lionweb.language.Annotation;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.Reference;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.MockPartitionObserver;
import io.lionweb.model.PartitionObserver;
import io.lionweb.model.ReferenceValue;
import io.lionweb.serialization.SimpleNode;
import io.lionweb.serialization.simplemath.IntLiteral;
import java.util.Arrays;
import org.junit.Test;

public class AbstractClassifierInstanceTest {

  @Test
  public void addAnnotation() {
    Annotation annotation = new Annotation();
    annotation.setID("annotation-A");

    SimpleNode n1 = new IntLiteral(1);
    AnnotationInstance ann1 = new DynamicAnnotationInstance("ann1", annotation);
    AnnotationInstance ann2 = new DynamicAnnotationInstance("ann2", annotation);
    AnnotationInstance ann1b = new DynamicAnnotationInstance("ann1", annotation);

    assertEquals(0, n1.getAnnotations().size());

    assertEquals(true, n1.addAnnotation(ann1));
    assertEquals(1, n1.getAnnotations().size());

    assertEquals(true, n1.addAnnotation(ann2));
    assertEquals(2, n1.getAnnotations().size());

    assertEquals(false, n1.addAnnotation(ann1b));
    assertEquals(2, n1.getAnnotations().size());

    assertEquals(false, n1.addAnnotation(ann1));
    assertEquals(2, n1.getAnnotations().size());
  }

  @Test
  public void removeAnnotation() {
    Annotation annotation = new Annotation();
    annotation.setID("annotation-A");

    SimpleNode n1 = new IntLiteral(1);
    AnnotationInstance ann1 = new DynamicAnnotationInstance("ann1", annotation);
    AnnotationInstance ann2 = new DynamicAnnotationInstance("ann2", annotation);
    AnnotationInstance ann3 = new DynamicAnnotationInstance("ann3", annotation);
    AnnotationInstance ann4 = new DynamicAnnotationInstance("ann4", annotation);
    AnnotationInstance ann5 = new DynamicAnnotationInstance("ann5", annotation);
    n1.addAnnotation(ann1);
    n1.addAnnotation(ann2);
    n1.addAnnotation(ann3);
    n1.addAnnotation(ann4);
    n1.addAnnotation(ann5);

    assertEquals(3, n1.removeAnnotation(ann4));
    assertEquals(3, n1.removeAnnotation(ann5));
    assertEquals(0, n1.removeAnnotation(ann1));
    assertEquals(1, n1.removeAnnotation(ann3));
    assertEquals(0, n1.removeAnnotation(ann2));

    assertThrows(IllegalArgumentException.class, () -> n1.removeAnnotation(ann1));
    assertThrows(IllegalArgumentException.class, () -> n1.removeAnnotation(ann2));
    assertThrows(IllegalArgumentException.class, () -> n1.removeAnnotation(ann3));
    assertThrows(IllegalArgumentException.class, () -> n1.removeAnnotation(ann4));
    assertThrows(IllegalArgumentException.class, () -> n1.removeAnnotation(ann5));
  }

  @Test
  public void addAndRemoveObservers() {
    SimpleNode n1 = new IntLiteral(1);

    PartitionObserver observer1 = new MockPartitionObserver();
    n1.registerPartitionObserver(observer1);
    assertEquals(false, n1.registerPartitionObserver(observer1));
    n1.unregisterPartitionObserver(observer1);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer1));
  }

  @Test
  public void addAndRemoveManyObservers() {
    SimpleNode n1 = new IntLiteral(1);

    PartitionObserver observer1 = new MockPartitionObserver();
    PartitionObserver observer2 = new MockPartitionObserver();
    PartitionObserver observer3 = new MockPartitionObserver();
    PartitionObserver observer4 = new MockPartitionObserver();
    PartitionObserver observer5 = new MockPartitionObserver();
    n1.registerPartitionObserver(observer1);
    n1.registerPartitionObserver(observer2);
    n1.registerPartitionObserver(observer3);
    n1.registerPartitionObserver(observer4);
    n1.registerPartitionObserver(observer5);
    n1.unregisterPartitionObserver(observer1);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer1));
    n1.unregisterPartitionObserver(observer2);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer1));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer2));
    n1.unregisterPartitionObserver(observer3);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer1));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer2));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer3));
    n1.unregisterPartitionObserver(observer4);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer1));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer2));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer3));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer4));
    n1.unregisterPartitionObserver(observer5);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer1));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer2));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer3));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer4));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterPartitionObserver(observer5));
  }

  @Test
  public void referenceObservabilityForReferred() {
    Annotation annotation = new Annotation();
    annotation.setID("annotation-A");

    Language language = new Language();
    language.setID("language-A");

    Concept c1 = new Concept();
    c1.setName("c1");
    c1.setID("c1-id");

    language.addElement(c1);

    Reference r1 = new Reference();
    r1.setName("r1");
    r1.setID("r1-id");
    r1.setKey("r1-key");
    c1.addFeature(r1);

    DynamicNode n1 = new DynamicNode("id-1", c1);
    DynamicNode n2 = new DynamicNode("id-2", c1);
    ReferenceValue rv1 = new ReferenceValue(null, "foo");
    int rv1Index = n1.addReferenceValue(r1, rv1);
    MockPartitionObserver observer1 = new MockPartitionObserver();
    n1.registerPartitionObserver(observer1);

    n1.setReferred(r1, rv1Index, n2);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceChangedRecord(
                n1, r1, 0, null, "foo", "id-2", "foo")),
        observer1.getRecords());

    n1.unregisterPartitionObserver(observer1);
    n1.setReferred(r1, rv1Index, n1);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceChangedRecord(
                n1, r1, 0, null, "foo", "id-2", "foo")),
        observer1.getRecords());

    n1.registerPartitionObserver(observer1);
    n1.setReferred(r1, rv1Index, n2);
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceChangedRecord(n1, r1, 0, null, "foo", "id-2", "foo"),
            new MockPartitionObserver.ReferenceChangedRecord(
                n1, r1, 0, "id-1", "foo", "id-2", "foo")),
        observer1.getRecords());
  }

  @Test
  public void referenceObservabilityForResolveInfo() {
    Annotation annotation = new Annotation();
    annotation.setID("annotation-A");

    Language language = new Language();
    language.setID("language-A");

    Concept c1 = new Concept();
    c1.setName("c1");
    c1.setID("c1-id");

    language.addElement(c1);

    Reference r1 = new Reference();
    r1.setName("r1");
    r1.setID("r1-id");
    r1.setKey("r1-key");
    c1.addFeature(r1);

    DynamicNode n1 = new DynamicNode("id-1", c1);
    ReferenceValue rv1 = new ReferenceValue(null, "foo");
    int rv1Index = n1.addReferenceValue(r1, rv1);
    MockPartitionObserver observer1 = new MockPartitionObserver();
    n1.registerPartitionObserver(observer1);

    n1.setResolveInfo(r1, rv1Index, "B");
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceChangedRecord(n1, r1, 0, null, "foo", null, "B")),
        observer1.getRecords());

    n1.unregisterPartitionObserver(observer1);
    n1.setResolveInfo(r1, rv1Index, "A");
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceChangedRecord(n1, r1, 0, null, "foo", null, "B")),
        observer1.getRecords());

    n1.registerPartitionObserver(observer1);
    n1.setResolveInfo(r1, rv1Index, "B");
    assertEquals(
        Arrays.asList(
            new MockPartitionObserver.ReferenceChangedRecord(n1, r1, 0, null, "foo", null, "B"),
            new MockPartitionObserver.ReferenceChangedRecord(n1, r1, 0, null, "A", null, "B")),
        observer1.getRecords());
  }
}
