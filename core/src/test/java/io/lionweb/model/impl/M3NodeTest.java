package io.lionweb.model.impl;

import static org.junit.Assert.assertEquals;

import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.MockClassifierInstanceObserver;
import io.lionweb.model.ReferenceValue;
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
    MockClassifierInstanceObserver observer = new MockClassifierInstanceObserver();
    language.addObserver(observer);

    // propertyChanged
    language.setName("MyLanguage");
    language.setName("MyOtherLanguage");
    Property name = LionCoreBuiltins.getINamed().getPropertyByName("name");
    assertEquals(
        Arrays.asList(
            new MockClassifierInstanceObserver.PropertyChangedRecord(
                language, name, null, "MyLanguage"),
            new MockClassifierInstanceObserver.PropertyChangedRecord(
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
            new MockClassifierInstanceObserver.ChildAddedRecord(language, entities, 0, c1),
            new MockClassifierInstanceObserver.ChildAddedRecord(language, entities, 1, c2)),
        observer.getRecords());
    observer.clearRecords();

    // childRemoved
    language.removeChild(c2);
    language.removeChild(c1);
    assertEquals(
        Arrays.asList(
            new MockClassifierInstanceObserver.ChildRemovedRecord(language, entities, 1, c2),
            new MockClassifierInstanceObserver.ChildRemovedRecord(language, entities, 0, c1)),
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
            new MockClassifierInstanceObserver.AnnotationAddedRecord(language, 0, ann1),
            new MockClassifierInstanceObserver.AnnotationAddedRecord(language, 1, ann2)),
        observer.getRecords());
    observer.clearRecords();

    // annotationRemoved
    language.removeAnnotation(ann2);
    language.removeAnnotation(ann1);
    assertEquals(
        Arrays.asList(
            new MockClassifierInstanceObserver.AnnotationRemovedRecord(language, 1, ann2),
            new MockClassifierInstanceObserver.AnnotationRemovedRecord(language, 0, ann1)),
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
            new MockClassifierInstanceObserver.ReferenceAddedRecord(
                language, languageDependsOn, new ReferenceValue(language2, "L2")),
            new MockClassifierInstanceObserver.ReferenceAddedRecord(
                language, languageDependsOn, new ReferenceValue(language3, "L3"))),
        observer.getRecords());
    observer.clearRecords();

    // referenceValueChanged
    ReferenceValue rvToL2 = language.getReferenceValues(languageDependsOn).get(0);
    ReferenceValue rvToL3 = language.getReferenceValues(languageDependsOn).get(1);
    rvToL2.setResolveInfo("Language 2");
    rvToL2.setReferred(new ProxyNode("12345"));
    rvToL3.setReferred(new ProxyNode("23456"));
    assertEquals(
        Arrays.asList(
            new MockClassifierInstanceObserver.ReferenceChangedRecord(
                language, languageDependsOn, 0, "l2", "L2", "l2", "Language 2"),
            new MockClassifierInstanceObserver.ReferenceChangedRecord(
                language, languageDependsOn, 0, "l2", "Language 2", "12345", "Language 2"),
            new MockClassifierInstanceObserver.ReferenceChangedRecord(
                language, languageDependsOn, 1, "l3", "L3", "23456", "L3")),
        observer.getRecords());
    observer.clearRecords();

    // referenceValueRemoved
    language.removeReferenceValue(languageDependsOn, 1);
    language.removeReferenceValue(languageDependsOn, 0);
    assertEquals(
        Arrays.asList(
            new MockClassifierInstanceObserver.ReferenceRemovedRecord(
                language, languageDependsOn, 1, "23456", "L3"),
            new MockClassifierInstanceObserver.ReferenceRemovedRecord(
                language, languageDependsOn, 0, "12345", "Language 2")),
        observer.getRecords());
    observer.clearRecords();
  }
}
