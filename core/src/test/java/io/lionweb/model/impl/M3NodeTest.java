package io.lionweb.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.MockPartitionObserver;
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
}
