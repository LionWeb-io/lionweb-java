package io.lionweb.model.impl;

import static org.junit.Assert.assertEquals;

import io.lionweb.language.Containment;
import io.lionweb.language.EnumerationLiteral;
import org.junit.Ignore;
import org.junit.Test;

public class M3NodeTest {

  @Test
  public void toStringEnumerationLiteralWithoutId() {
    EnumerationLiteral literal = new EnumerationLiteral();
    assertEquals("EnumerationLiteral[null]", literal.toString());
  }

  @Test
  public void toStringEnumerationLiteralIncludingId() {
    EnumerationLiteral literal = new EnumerationLiteral();
    literal.setID("123");
    assertEquals("EnumerationLiteral[123]", literal.toString());
  }

  @Test
  @Ignore("Inconsistency to be discussed")
  public void toStringContainmentWithoutId() {
    Containment containment = new Containment();
    assertEquals("Containment[null]", containment.toString());
  }

  @Test
  @Ignore("Inconsistency to be discussed")
  public void toStringContainmentIncludingId() {
    Containment containment = new Containment();
    containment.setID("asdf");
    assertEquals("Containment[asdf]", containment.toString());
  }
}
