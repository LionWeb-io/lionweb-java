package io.lionweb.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
  public static @Nonnull String cleanString(@Nonnull String string) {
    Objects.requireNonNull(string, "string cannot be null");
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
  public static boolean isValidID(@Nullable String id) {
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

  /**
   * Encodes an arbitrary string into a valid LionWeb ID using Base64 URL encoding without padding.
   * This guarantees a lossless round-trip: the original string can be recovered via {@link
   * #decodeFromValidId(String)}. Unlike {@link #cleanString(String)}, this method avoids collisions
   * between different source strings.
   *
   * @param string the input string to encode; must not be null
   * @return a Base64 URL-encoded string that is a valid LionWeb identifier
   */
  public static @Nonnull String encodeToValidId(@Nonnull String string) {
    Objects.requireNonNull(string, "string cannot be null");
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(string.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decodes a Base64 URL-encoded string produced by {@link #encodeToValidId(String)} back to the
   * original string.
   *
   * @param id the Base64 URL-encoded identifier to decode; must not be null
   * @return the original string that was encoded
   * @throws IllegalArgumentException if the input is not valid Base64 URL-encoded data
   */
  public static @Nonnull String decodeFromValidId(@Nonnull String id) {
    Objects.requireNonNull(id, "id cannot be null");
    return new String(Base64.getUrlDecoder().decode(id), StandardCharsets.UTF_8);
  }
}
