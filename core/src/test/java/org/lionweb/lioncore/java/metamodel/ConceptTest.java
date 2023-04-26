package org.lionweb.lioncore.java.metamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConceptTest {

  @Test
  public void aConceptIsNonAbstractByDefault() {
    Concept c = new Concept();
    assertEquals(false, c.isAbstract());
  }
}
