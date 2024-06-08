package io.lionweb.lioncore.java.api;

import static org.junit.Assert.assertEquals;

import io.lionweb.lioncore.java.language.Concept;
import org.junit.Test;

public class CompositeClassifierInstanceResolverTest {

  @Test
  public void emptyCompositeClassifier() {
    Concept concept = new Concept();

    CompositeClassifierInstanceResolver instanceResolver =
        new CompositeClassifierInstanceResolver();
    assertEquals(null, instanceResolver.resolve("Foo"));
    assertEquals("CompositeClassifierInstanceResolver([])", instanceResolver.toString());
  }
}
