package io.lionweb.language;

import static org.junit.Assert.assertEquals;

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
}
