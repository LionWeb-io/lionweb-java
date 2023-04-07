package org.lionweb.lioncore.java.metamodel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class MetamodelTest {

  @Test
  public void addDependency() {
    Metamodel m1 = new Metamodel("m1");
    Metamodel m2 = new Metamodel("m2");
    Metamodel m3 = new Metamodel("m3");

    assertEquals(Collections.emptyList(), m1.dependsOn());
    m1.addDependency(m2);
    assertEquals(Arrays.asList(m2), m1.dependsOn());
    m1.addDependency(m3);
    assertEquals(Arrays.asList(m2, m3), m1.dependsOn());
  }
}
