package io.lionweb.utils;

/**
 * Utility class providing methods for processing and sanitizing strings. This class is designed to
 * centralize reusable logic related to identifier handling.
 *
 * <p>The class is non-instantiable and only contains static methods.
 */
public class IdUtils {
  private IdUtils() {
    // Prevent instantiation
  }

  /**
   * Cleans the input string by replacing all non-alphanumeric characters, except underscores ('_')
   * and hyphens ('-'), with a hyphen ('-'). This method is useful for sanitizing strings to match
   * identifier requirements.
   *
   * @param string the input string to sanitize; must not be null
   * @return a sanitized version of the input string where invalid characters are replaced with a
   *     hyphen
   */
  public static String cleanString(String string) {
    return string.replaceAll("[^a-zA-Z0-9_-]", "-");
  }

  /**
   * Validates whether the provided string is a valid identifier. A valid identifier must not be
   * null or empty and can only contain alphanumeric characters (a-z, A-Z, 0-9), underscores ('_'),
   * or hyphens ('-').
   *
   * @param id the string to be validated as an identifier
   * @return {@code true} if the string is a valid identifier; {@code false} otherwise
   */
  public static boolean isValidID(String id) {
    if (id == null || id.isEmpty()) {
      return false;
    }
    for (int i = 0; i < id.length(); i++) {
      char c = id.charAt(i);
      if (!((c >= 'a' && c <= 'z')
          || (c >= 'A' && c <= 'Z')
          || (c >= '0' && c <= '9')
          || c == '_'
          || c == '-')) {
        return false;
      }
    }
    return true;
  }
}
