package io.lionweb.model.impl;

import static org.junit.Assert.assertEquals;

import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.MockNodeObserver;
import java.util.Arrays;
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
    MockNodeObserver observer = new MockNodeObserver();
    language.setObserver(observer);

    // propertyChanged
    language.setName("MyLanguage");
    language.setName("MyOtherLanguage");
    Property name = LionCoreBuiltins.getINamed().getPropertyByName("name");
    assertEquals(
        Arrays.asList(
            new MockNodeObserver.PropertyChangedRecord(language, name, null, "MyLanguage"),
            new MockNodeObserver.PropertyChangedRecord(
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
            new MockNodeObserver.ChildAddedRecord(language, entities, 0, c1),
            new MockNodeObserver.ChildAddedRecord(language, entities, 1, c2)),
        observer.getRecords());
    observer.clearRecords();

    // childRemoved
    language.removeChild(c2);
    language.removeChild(c1);
    assertEquals(
        Arrays.asList(
            new MockNodeObserver.ChildRemovedRecord(language, entities, 1, c2),
            new MockNodeObserver.ChildRemovedRecord(language, entities, 0, c1)),
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
            new MockNodeObserver.AnnotationAddedRecord(language, 0, ann1),
            new MockNodeObserver.AnnotationAddedRecord(language, 1, ann2)),
        observer.getRecords());
    observer.clearRecords();

    // annotationRemoved
    language.removeAnnotation(ann2);
    language.removeAnnotation(ann1);
    assertEquals(
        Arrays.asList(
            new MockNodeObserver.AnnotationRemovedRecord(language, 1, ann2),
            new MockNodeObserver.AnnotationRemovedRecord(language, 0, ann1)),
        observer.getRecords());
    observer.clearRecords();

    // TODO referenceValueAdded
    Language language2 = new Language();
    language2.setID("l2");
    Language language3 = new Language();
    language3.setID("l3");

    language.addDependency(language2);
    language.addDependency(language3);

    // TODO referenceValueChanged

    // TODO referenceValueRemoved
  }
}
