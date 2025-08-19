package io.lionweb.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import io.lionweb.language.Annotation;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.Reference;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.ClassifierInstanceObserver;
import io.lionweb.model.MockClassifierInstanceObserver;
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

    ClassifierInstanceObserver observer1 = new MockClassifierInstanceObserver();
    n1.registerObserver(observer1);
    assertThrows(IllegalArgumentException.class, () -> n1.registerObserver(observer1));
    n1.unregisterObserver(observer1);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer1));
  }

  @Test
  public void addAndRemoveManyObservers() {
    SimpleNode n1 = new IntLiteral(1);

    ClassifierInstanceObserver observer1 = new MockClassifierInstanceObserver();
    ClassifierInstanceObserver observer2 = new MockClassifierInstanceObserver();
    ClassifierInstanceObserver observer3 = new MockClassifierInstanceObserver();
    ClassifierInstanceObserver observer4 = new MockClassifierInstanceObserver();
    ClassifierInstanceObserver observer5 = new MockClassifierInstanceObserver();
    n1.registerObserver(observer1);
    n1.registerObserver(observer2);
    n1.registerObserver(observer3);
    n1.registerObserver(observer4);
    n1.registerObserver(observer5);
    n1.unregisterObserver(observer1);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer1));
    n1.unregisterObserver(observer2);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer1));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer2));
    n1.unregisterObserver(observer3);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer1));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer2));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer3));
    n1.unregisterObserver(observer4);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer1));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer2));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer3));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer4));
    n1.unregisterObserver(observer5);
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer1));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer2));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer3));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer4));
    assertThrows(IllegalArgumentException.class, () -> n1.unregisterObserver(observer5));
  }

  @Test
  public void referenceObservability() {
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
    ReferenceValue rv1 = new ReferenceValue();
    rv1.setResolveInfo("foo");
    n1.addReferenceValue(r1, rv1);
    MockClassifierInstanceObserver observer1 = new MockClassifierInstanceObserver();
    n1.registerObserver(observer1);

    rv1.setReferred(n2);
    assertEquals(
        Arrays.asList(
            new MockClassifierInstanceObserver.ReferenceChangedRecord(
                n1, r1, 0, null, "foo", "id-2", "foo")),
        observer1.getRecords());

    n1.unregisterObserver(observer1);
    rv1.setReferred(n1);
    assertEquals(
        Arrays.asList(
            new MockClassifierInstanceObserver.ReferenceChangedRecord(
                n1, r1, 0, null, "foo", "id-2", "foo")),
        observer1.getRecords());

    n1.registerObserver(observer1);
    rv1.setReferred(n2);
    assertEquals(
        Arrays.asList(
            new MockClassifierInstanceObserver.ReferenceChangedRecord(
                n1, r1, 0, null, "foo", "id-2", "foo"),
            new MockClassifierInstanceObserver.ReferenceChangedRecord(
                n1, r1, 0, null, "foo", "id-2", "foo")),
        observer1.getRecords());
  }
}
