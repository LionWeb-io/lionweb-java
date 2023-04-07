package org.lionweb.lioncore.java.utils;

import java.util.regex.Pattern;

public class Naming {
  public static void validateQualifiedName(String qualifiedName) {
    if (!Pattern.matches("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*", qualifiedName)) {
      throw new InvalidName("qualified name", qualifiedName);
    }
  }

  public static void validatename(String name) {
    if (name == null) {
      throw new IllegalArgumentException("The name should not be null");
    }
    if (!Pattern.matches("[a-zA-Z][a-zA-Z0-9_]*", name)) {
      throw new InvalidName("simple name", name);
    }
  }
}
