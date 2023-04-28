package org.lionweb.lioncore.java.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonChecks {

  private static final Pattern ID_Pattern = Pattern.compile("[a-zA-Z0-9_-]+");

  public static boolean isValidID(String id) {
    if (id == null) {
      return false;
    }
    Matcher m = ID_Pattern.matcher(id);
    return m.matches();
  }
}
