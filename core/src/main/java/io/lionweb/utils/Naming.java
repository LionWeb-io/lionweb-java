package io.lionweb.utils;

import java.util.regex.Pattern;

public class Naming {

  private Naming() {
    // Prevent instantiation
  }

  public static void validateQualifiedName(String qualifiedName) {
    if (!Pattern.matches("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*", qualifiedName)) {
      throw new InvalidName("qualified name", qualifiedName);
    }
  }

  public static void validateName(String name) {
    if (name == null) {
      throw new IllegalArgumentException("The name should not be null");
    }
    if (!Pattern.matches("[a-zA-Z][a-zA-Z0-9_]*", name)) {
      throw new InvalidName("simple name", name);
    }
  }
}
