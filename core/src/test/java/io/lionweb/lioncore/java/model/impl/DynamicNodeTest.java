package io.lionweb.lioncore.java.model.impl;

import static org.junit.Assert.*;

import com.google.gson.JsonArray;
import io.lionweb.lioncore.java.language.Annotation;
import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.serialization.MyNodeWithProperties;
import java.util.Arrays;
import org.junit.Test;

public class DynamicNodeTest {

  @Test
  public void equalityPositiveCaseEmptyNodes() {
    MyNodeWithProperties n1 = new MyNodeWithProperties("id1");
    MyNodeWithProperties n2 = new MyNodeWithProperties("id1");
    assertTrue(n1.equals(n2));
  }

  @Test
  public void equalityNegativeCaseEmptyNodes() {
    MyNodeWithProperties n1 = new MyNodeWithProperties("id1");
    MyNodeWithProperties n2 = new MyNodeWithProperties("id2");
    assertFalse(n1.equals(n2));
  }

  @Test
  public void equalityPositiveCaseWithProperties() {
    MyNodeWithProperties n1 = new MyNodeWithProperties("id1");
    n1.setP1(true);
    n1.setP2(123);
    n1.setP3("foo");
    n1.setP4(new JsonArray());
    MyNodeWithProperties n2 = new MyNodeWithProperties("id1");
    n2.setP1(true);
    n2.setP2(123);
    n2.setP3("foo");
    n2.setP4(new JsonArray());
    assertTrue(n1.equals(n2));
  }

  @Test
  public void equalityNegativrCaseWithProperties() {
    MyNodeWithProperties n1 = new MyNodeWithProperties("id1");
    n1.setP1(true);
    n1.setP2(123);
    n1.setP3("foo");
    n1.setP4(new JsonArray());
    MyNodeWithProperties n2 = new MyNodeWithProperties("id1");
    n2.setP1(true);
    n2.setP2(123);
    n2.setP3("bar");
    n2.setP4(new JsonArray());
    assertFalse(n1.equals(n2));
  }

  @Test
  public void removeChildOnSingleContainment() {
    Concept c = new Concept();
    Containment containment = Containment.createOptional("ch", c);
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
    assertEquals(null, a2_3.getParent());
    assertEquals(Arrays.asList(a1_1, a1_2, a2_4), n1.getAnnotations());
    assertEquals(Arrays.asList(a1_1, a1_2), n1.getAnnotations(a1));
    assertEquals(Arrays.asList(a2_4), n1.getAnnotations(a2));
  }
}
