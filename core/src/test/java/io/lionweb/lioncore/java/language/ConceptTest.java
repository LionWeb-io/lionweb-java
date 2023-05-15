package io.lionweb.lioncore.java.language;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConceptTest {

  @Test
  public void aConceptIsNonAbstractByDefault() {
    Concept c = new Concept();
    assertEquals(false, c.isAbstract());
  }
}
