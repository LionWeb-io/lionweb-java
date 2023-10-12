package io.lionweb.lioncore.java.language;

import static org.junit.Assert.assertEquals;

import java.util.*;
import org.junit.Test;

public class ClassifierAncestorsTest {
  @Test
  public void concept() {
    Concept a = new Concept("A");
    Concept b = new Concept("b");
    ConceptInterface i = new ConceptInterface("i");
    ConceptInterface j = new ConceptInterface("j");
    ConceptInterface k = new ConceptInterface("k");
    ConceptInterface l = new ConceptInterface("l");
    a.setExtendedConcept(b);
    a.addImplementedInterface(i);
    a.addImplementedInterface(j);
    i.addExtendedInterface(k);
    b.addImplementedInterface(l);
    k.addExtendedInterface(l);

    assertEquals(new HashSet<>(Arrays.asList(b, i, j)), new HashSet<>(a.directAncestors()));
    assertEquals(new HashSet<>(Arrays.asList(b, i, j, k, l)), new HashSet<>(a.allAncestors()));
  }

  @Test
  public void annotation() {
    Annotation a = new Annotation();
    a.setName("A");
    Annotation b = new Annotation();
    b.setName("b");
    ConceptInterface i = new ConceptInterface("i");
    ConceptInterface j = new ConceptInterface("j");
    ConceptInterface k = new ConceptInterface("k");
    ConceptInterface l = new ConceptInterface("l");
    a.setExtendedAnnotation(b);
    a.addImplementedInterface(i);
    a.addImplementedInterface(j);
    i.addExtendedInterface(k);
    b.addImplementedInterface(l);
    k.addExtendedInterface(l);

    assertEquals(new HashSet<>(Arrays.asList(b, i, j)), new HashSet<>(a.directAncestors()));
    assertEquals(new HashSet<>(Arrays.asList(b, i, j, k, l)), new HashSet<>(a.allAncestors()));
  }

  @Test
  public void iface() {
    ConceptInterface i = new ConceptInterface("i");
    ConceptInterface j = new ConceptInterface("j");
    ConceptInterface k = new ConceptInterface("k");
    ConceptInterface l = new ConceptInterface("l");
    i.addExtendedInterface(j);
    i.addExtendedInterface(k);
    k.addExtendedInterface(l);
    k.addExtendedInterface(j);

    assertEquals(new HashSet<>(Arrays.asList(j, k)), new HashSet<>(i.directAncestors()));
    assertEquals(new HashSet<>(Arrays.asList(j, k, l)), new HashSet<>(i.allAncestors()));
  }

  @Test
  public void ifaceSelf() {
    ConceptInterface i = new ConceptInterface("i");
    ConceptInterface j = new ConceptInterface("j");
    ConceptInterface k = new ConceptInterface("k");
    ConceptInterface l = new ConceptInterface("l");
    i.addExtendedInterface(i);
    i.addExtendedInterface(j);
    i.addExtendedInterface(k);
    k.addExtendedInterface(l);
    k.addExtendedInterface(j);

    assertEquals(new HashSet<>(Arrays.asList(i, j, k)), new HashSet<>(i.directAncestors()));
    assertEquals(new HashSet<>(Arrays.asList(i, j, k, l)), new HashSet<>(i.allAncestors()));
  }

  @Test
  public void ifaceDirectLoop() {
    ConceptInterface i = new ConceptInterface("i");
    ConceptInterface j = new ConceptInterface("j");
    ConceptInterface k = new ConceptInterface("k");
    ConceptInterface l = new ConceptInterface("l");
    i.addExtendedInterface(j);
    j.addExtendedInterface(i);
    i.addExtendedInterface(k);
    k.addExtendedInterface(l);
    k.addExtendedInterface(j);

    assertEquals(new HashSet<>(Arrays.asList(j, k)), new HashSet<>(i.directAncestors()));
    assertEquals(new HashSet<>(Arrays.asList(i, j, k, l)), new HashSet<>(i.allAncestors()));
  }

  @Test
  public void ifaceIndirectLoop() {
    ConceptInterface i = new ConceptInterface("i");
    ConceptInterface j = new ConceptInterface("j");
    ConceptInterface k = new ConceptInterface("k");
    ConceptInterface l = new ConceptInterface("l");
    i.addExtendedInterface(j);
    i.addExtendedInterface(k);
    k.addExtendedInterface(l);
    k.addExtendedInterface(j);
    l.addExtendedInterface(i);

    assertEquals(new HashSet<>(Arrays.asList(j, k)), new HashSet<>(i.directAncestors()));
    assertEquals(new HashSet<>(Arrays.asList(i, j, k, l)), new HashSet<>(i.allAncestors()));
  }
}
