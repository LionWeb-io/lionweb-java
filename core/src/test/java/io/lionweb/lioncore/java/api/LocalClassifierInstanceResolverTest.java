package io.lionweb.lioncore.java.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import java.util.Arrays;
import org.junit.Test;

public class LocalClassifierInstanceResolverTest {

  @Test
  public void resolveOrProxyWhenCannotBeSolved() {
    Concept concept = new Concept();

    LocalClassifierInstanceResolver lcir = new LocalClassifierInstanceResolver();
    lcir.add(new DynamicNode("123", concept));

    assertEquals(new ProxyNode("unexistingID"), lcir.resolveOrProxy("unexistingID"));
  }

  @Test
  public void resolveOrProxyWhenCanBeSolved() {
    Concept concept = new Concept();

    LocalClassifierInstanceResolver lcir = new LocalClassifierInstanceResolver();
    DynamicNode n123 = new DynamicNode("123", concept);
    lcir.add(n123);

    assertSame(n123, lcir.resolveOrProxy("123"));
  }

  @Test
  public void varargConstructor() {
    Concept concept = new Concept();

    DynamicNode n123 = new DynamicNode("123", concept);
    DynamicNode n456 = new DynamicNode("456", concept);
    DynamicNode n789 = new DynamicNode("789", concept);

    LocalClassifierInstanceResolver lcir = new LocalClassifierInstanceResolver(n123, n456, n789);

    assertEquals(null, lcir.resolve("unexistingID"));
    assertSame(n123, lcir.resolveOrProxy("123"));
    assertSame(n456, lcir.resolveOrProxy("456"));
    assertSame(n789, lcir.resolveOrProxy("789"));
  }

  @Test
  public void listConstructor() {
    Concept concept = new Concept();

    DynamicNode n123 = new DynamicNode("123", concept);
    DynamicNode n456 = new DynamicNode("456", concept);
    DynamicNode n789 = new DynamicNode("789", concept);

    LocalClassifierInstanceResolver lcir =
        new LocalClassifierInstanceResolver(Arrays.asList(n123, n456, n789));

    assertEquals(null, lcir.resolve("unexistingID"));
    assertSame(n123, lcir.resolveOrProxy("123"));
    assertSame(n456, lcir.resolveOrProxy("456"));
    assertSame(n789, lcir.resolveOrProxy("789"));
  }
}
