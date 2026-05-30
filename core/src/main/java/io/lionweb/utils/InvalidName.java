package io.lionweb.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exception thrown when an invalid name value is encountered during validation.
 *
 * <p>This exception is used to signal that a provided name does not conform to the required format
 * or expected constraints (e.g., a simple name or a qualified name).
 *
 * <p>The exception message includes both the type of the name (such as "simple name" or "qualified
 * name") and the invalid value for additional context.
 */
public class InvalidName extends RuntimeException {

  /**
   * Constructs an {@code InvalidName} exception with a descriptive error message.
   *
   * @param nameType the type of the name being validated (e.g., "simple name", "qualified name");
   *     must not be null
   * @param value the invalid name value that caused the exception to be raised; must not be null
   */
  public InvalidName(@Nonnull String nameType, @Nullable String value) {
    super("The given name is not a valid " + nameType + ". Value: '" + value + "'");
  }
}
