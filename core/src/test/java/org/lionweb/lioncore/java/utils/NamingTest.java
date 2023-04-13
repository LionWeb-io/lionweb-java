package org.lionweb.lioncore.java.utils;

import org.junit.Test;

public class NamingTest {

  @Test
  public void validSimpleName() {
    Naming.validateName("myID123");
  }

  @Test(expected = InvalidName.class)
  public void invalidSimpleNameStartingWithDigits() {
    Naming.validateName("1myID");
  }

  @Test
  public void validQualifiedName() {
    Naming.validateQualifiedName("myID123.a.b");
  }
}
