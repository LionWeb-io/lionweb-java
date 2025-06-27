package io.lionweb.language;

import static org.junit.Assert.assertEquals;

import java.util.*;
import org.junit.Test;

public class ClassifierAncestorsTest {
  @Test
  public void concept() {
    Concept a = new Concept("A");
    Concept b = new Concept("b");
    Interface i = new Interface("i");
    Interface j = new Interface("j");
    Interface k = new Interface("k");
    Interface l = new Interface("l");
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
    Interface i = new Interface("i");
    Interface j = new Interface("j");
    Interface k = new Interface("k");
    Interface l = new Interface("l");
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
    Interface i = new Interface("i");
    Interface j = new Interface("j");
    Interface k = new Interface("k");
    Interface l = new Interface("l");
    i.addExtendedInterface(j);
    i.addExtendedInterface(k);
    k.addExtendedInterface(l);
    k.addExtendedInterface(j);

    assertEquals(new HashSet<>(Arrays.asList(j, k)), new HashSet<>(i.directAncestors()));
    assertEquals(new HashSet<>(Arrays.asList(j, k, l)), new HashSet<>(i.allAncestors()));
  }

  @Test
  public void ifaceSelf() {
    Interface i = new Interface("i");
    Interface j = new Interface("j");
    Interface k = new Interface("k");
    Interface l = new Interface("l");
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
    Interface i = new Interface("i");
    Interface j = new Interface("j");
    Interface k = new Interface("k");
    Interface l = new Interface("l");
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
    Interface i = new Interface("i");
    Interface j = new Interface("j");
    Interface k = new Interface("k");
    Interface l = new Interface("l");
    i.addExtendedInterface(j);
    i.addExtendedInterface(k);
    k.addExtendedInterface(l);
    k.addExtendedInterface(j);
    l.addExtendedInterface(i);

    assertEquals(new HashSet<>(Arrays.asList(j, k)), new HashSet<>(i.directAncestors()));
    assertEquals(new HashSet<>(Arrays.asList(i, j, k, l)), new HashSet<>(i.allAncestors()));
  }
}
