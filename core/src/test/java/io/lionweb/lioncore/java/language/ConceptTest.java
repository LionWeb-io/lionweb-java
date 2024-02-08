package io.lionweb.lioncore.java.language;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConceptTest {

  @Test
  public void aConceptIsNonAbstractByDefault() {
    Concept c = new Concept();
    assertEquals(false, c.isAbstract());
  }

  @Test
  public void featuresAndInheritanceCycles() {
    Language l = new Language("MyLanguage", "l-id", "l-key", "123");
    Concept a = new Concept(l, "A", "a-id", "a-key");
    Concept b = new Concept(l, "B", "b-id", "b-key");
    Concept c = new Concept(l, "C", "c-id", "c-key");
    a.setExtendedConcept(c);
    b.setExtendedConcept(a);
    c.setExtendedConcept(b);
    b.addFeature(new Property("P1", b, "p1-id").setKey("p1-key"));

    assertEquals(1, a.allFeatures().size());
    assertEquals(1, a.inheritedFeatures().size());
    assertEquals(1, b.allFeatures().size());
    assertEquals(1, b.inheritedFeatures().size());
    assertEquals(1, c.allFeatures().size());
    assertEquals(1, c.inheritedFeatures().size());
  }

  @Test
  public void checkDuplicateInheritance() {
    Language l = new Language("MyLanguage", "l-id", "l-key", "123");
    Interface a = new Interface(l, "A", "a-id", "a-key");
    Interface b = new Interface(l, "B", "b-id", "b-key");
    Interface c = new Interface(l, "C", "c-id", "c-key");
    Interface d = new Interface(l, "D", "d-id", "d-key");
    b.addExtendedInterface(d);
    c.addExtendedInterface(d);
    a.addExtendedInterface(b);
    a.addExtendedInterface(c);
    d.addFeature(new Property("P1", d, "p1-id").setKey("p1-key"));

    assertEquals(1, a.allFeatures().size());
    assertEquals(1, a.inheritedFeatures().size());
    assertEquals(1, b.allFeatures().size());
    assertEquals(1, b.inheritedFeatures().size());
    assertEquals(1, c.allFeatures().size());
    assertEquals(1, c.inheritedFeatures().size());
    assertEquals(1, d.allFeatures().size());
    assertEquals(0, d.inheritedFeatures().size());
  }
}
