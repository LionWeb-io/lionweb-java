package io.lionweb.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class NamingTest {

  @Test
  public void validSimpleName() {
    Naming.validateName("myID123");
  }

  @Test
  public void invalidSimpleNameStartingWithDigits() {
    assertThrows(InvalidName.class, () -> Naming.validateName("1myID"));
  }

  @Test
  public void validQualifiedName() {
    Naming.validateQualifiedName("myID123.a.b");
  }
}
