package org.lionweb.lioncore.java.utils;

import java.util.regex.Pattern;

public class CommonChecks {
  public static boolean isValidID(String id) {
    return id != null && Pattern.matches("[a-zA-Z0-9_-]+", id);
  }
}
