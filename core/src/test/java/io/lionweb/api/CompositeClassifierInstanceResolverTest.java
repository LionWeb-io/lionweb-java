package io.lionweb.api;

import static org.junit.Assert.assertEquals;

import io.lionweb.language.Concept;
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
