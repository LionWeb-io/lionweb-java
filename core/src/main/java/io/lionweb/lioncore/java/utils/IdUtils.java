package io.lionweb.lioncore.java.utils;

public class IdUtils {
  private IdUtils() {
    // Prevent instantiation
  }

  public static String cleanString(String string) {
    return string.replaceAll("\\.", "-");
  }
}
