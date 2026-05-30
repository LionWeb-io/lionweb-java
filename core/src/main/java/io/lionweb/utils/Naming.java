package io.lionweb.utils;

import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * Utility class for validating naming conventions and structures. This class is designed to
 * centralize reusable logic for name validation, including simple names and qualified names.
 *
 * <p>A simple name adheres to the pattern of starting with an alphabetical character (a-z, A-Z),
 * followed by zero or more alphanumeric characters or underscores ('_'). A qualified name extends
 * this rule to use the dot ('.') character as a separator between valid simple names.
 *
 * <p>The {@code Naming} class is non-instantiable and only contains static validation methods.
 */
public class Naming {

  private Naming() {
    // Prevent instantiation
  }

  /**
   * Validates that the provided string is a properly formatted qualified name. A qualified name
   * must start with an alphabetical character (a-z, A-Z), which may be followed by zero or more
   * alphanumeric characters or underscores ('_'). The name can contain multiple segments separated
   * by periods ('.'), with each segment conforming to the same rules as a simple name.
   *
   * @param qualifiedName the string to validate as a qualified name; must not be null
   * @throws InvalidName if the provided string does not conform to the qualified name format
   */
  public static void validateQualifiedName(@Nonnull String qualifiedName) {
    Objects.requireNonNull(qualifiedName, "qualifiedName should not be null");
    if (!Pattern.matches("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*", qualifiedName)) {
      throw new InvalidName("qualified name", qualifiedName);
    }
  }

  /**
   * Validates that the provided string conforms to the format of a simple name. A simple name
   * starts with an alphabetical character (a-z, A-Z) followed by zero or more alphanumeric
   * characters or underscores ('_').
   *
   * @param name the string to validate as a simple name; must not be null
   * @throws InvalidName if the provided string does not conform to the simple name format
   */
  public static void validateName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    if (!Pattern.matches("[a-zA-Z][a-zA-Z0-9_]*", name)) {
      throw new InvalidName("simple name", name);
    }
  }
}
