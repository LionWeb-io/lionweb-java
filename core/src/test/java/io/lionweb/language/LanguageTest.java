package io.lionweb.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class LanguageTest {

  @Test
  public void addDependency() {
    Language m1 = new Language("m1");
    Language m2 = new Language("m2");
    Language m3 = new Language("m3");

    assertEquals(Collections.emptyList(), m1.dependsOn());
    m1.addDependency(m2);
    assertEquals(Arrays.asList(m2), m1.dependsOn());
    m1.addDependency(m3);
    assertEquals(Arrays.asList(m2, m3), m1.dependsOn());
  }

  @Test
  public void getAnnotationByName() {
    Language m1 = new Language("m1");

    Language m2 = new Language("m2");
    Annotation a1inM2 = new Annotation(m2, "A1");

    Language m3 = new Language("m3");
    Annotation a2inM3 = new Annotation(m3, "A2");

    assertNull(m1.getAnnotationByName("A1"));
    assertNull(m1.getAnnotationByName("A2"));

    assertEquals(a1inM2, m2.getAnnotationByName("A1"));
    assertNull(m2.getAnnotationByName("A2"));

    assertNull(m3.getAnnotationByName("A1"));
    assertEquals(a2inM3, m3.getAnnotationByName("A2"));
  }
}
