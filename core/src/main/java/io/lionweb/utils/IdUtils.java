package io.lionweb.utils;

public class IdUtils {
  private IdUtils() {
    // Prevent instantiation
  }

  public static String cleanString(String string) {
    return string.replaceAll("[^a-zA-Z0-9_-]", "-");
  }
}
