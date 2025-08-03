package io.lionweb.model.impl;

import static org.junit.Assert.assertEquals;

import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;
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

    // TODO childRemoved

    // TODO annotationAdded

    // TODO annotationRemoved

    // TODO referenceValueAdded

    // TODO referenceValueChanged

    // TODO referenceValueRemoved
  }
}
