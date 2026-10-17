package io.lionweb.utils;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for validating naming conventions and structures. This class is designed to
 * centralize reusable logic for name validation, including simple names and qualified names.
 *
 * <p>A simple name is a Java identifier not containing the dollar sign ('$'), as required by the
 * LionWeb specification. Java keywords and Unicode letters are allowed. A qualified name extends
 * this rule to use the dot ('.') character as a separator between valid simple names.
 *
 * <p>The {@code Naming} class is non-instantiable and only contains static validation methods.
 */
public class Naming {

  private Naming() {
    // Prevent instantiation
  }

  /**
   * Validates that the provided string is a properly formatted qualified name: one or more valid
   * simple names separated by periods ('.').
   *
   * @param qualifiedName the string to validate as a qualified name; must not be null
   * @throws InvalidName if the provided string does not conform to the qualified name format
   */
  public static void validateQualifiedName(@Nonnull String qualifiedName) {
    Objects.requireNonNull(qualifiedName, "qualifiedName should not be null");
    if (!isValidQualifiedName(qualifiedName)) {
      throw new InvalidName("qualified name", qualifiedName);
    }
  }

  /**
   * Checks whether the provided string is one or more valid simple names separated by periods
   * ('.').
   */
  public static boolean isValidQualifiedName(@Nullable String qualifiedName) {
    if (qualifiedName == null) {
      return false;
    }
    for (String segment : qualifiedName.split("\\.", -1)) {
      if (!isValidName(segment)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Validates that the provided string conforms to the format of a simple name, as defined by
   * {@link #isValidName(String)}.
   *
   * @param name the string to validate as a simple name; must not be null
   * @throws InvalidName if the provided string does not conform to the simple name format
   */
  public static void validateName(@Nonnull String name) {
    Objects.requireNonNull(name, "name should not be null");
    if (!isValidName(name)) {
      throw new InvalidName("simple name", name);
    }
  }

  /**
   * Checks whether the provided string is a valid simple name: a non-empty Java identifier not
   * containing the dollar sign ('$'). Java keywords and Unicode letters are allowed.
   */
  public static boolean isValidName(@Nullable String name) {
    if (name == null || name.isEmpty()) {
      return false;
    }
    int i = 0;
    while (i < name.length()) {
      int c = name.codePointAt(i);
      if (c == '$'
          || Character.isIdentifierIgnorable(c)
          || (i == 0 ? !Character.isJavaIdentifierStart(c) : !Character.isJavaIdentifierPart(c))) {
        return false;
      }
      i += Character.charCount(c);
    }
    return true;
  }
}
