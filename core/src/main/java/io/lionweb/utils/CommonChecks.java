package io.lionweb.utils;

public class CommonChecks {

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
