package io.lionweb.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class LanguageTest {

  @Test
  public void addDependency() {
    Language l1 = new Language("l1");
    Language l2 = new Language("l2");
    Language l3 = new Language("l3");

    assertEquals(Collections.emptyList(), l1.dependsOn());
    l1.addDependency(l2);
    assertEquals(Arrays.asList(l2), l1.dependsOn());
    l1.addDependency(l3);
    assertEquals(Arrays.asList(l2, l3), l1.dependsOn());
  }

  @Test
  public void getAnnotationByName() {
    Language l1 = new Language("l1");

    Language l2 = new Language("l2");
    Annotation a1inM2 = new Annotation(l2, "A1");

    Language l3 = new Language("l3");
    Annotation a2inM3 = new Annotation(l3, "A2");

    assertNull(l1.getAnnotationByName("A1"));
    assertNull(l1.getAnnotationByName("A2"));

    assertEquals(a1inM2, l2.getAnnotationByName("A1"));
    assertNull(l2.getAnnotationByName("A2"));

    assertNull(l3.getAnnotationByName("A1"));
    assertEquals(a2inM3, l3.getAnnotationByName("A2"));
  }
}
