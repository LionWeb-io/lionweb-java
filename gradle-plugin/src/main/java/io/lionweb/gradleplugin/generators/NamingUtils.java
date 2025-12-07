package io.lionweb.gradleplugin.generators;

import io.lionweb.language.Language;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

class NamingUtils {

  static String capitalize(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  static String camelCase(String s) {
    if (s == null || s.isEmpty()) {
      return s;
    }

    if (s.equals(s.toUpperCase())) {
      s = s.toLowerCase();
    }

    String[] rawParts = s.trim().split("[^A-Za-z0-9]+");
    List<String> parts = new ArrayList<>();

    // Further split each raw part by uppercase boundaries
    for (String p : rawParts) {
      if (p.isEmpty()) continue;
      String[] sub = p.split("(?=[A-Z])");
      for (String x : sub) {
        if (!x.isEmpty()) {
          parts.add(x);
        }
      }
    }

    if (parts.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder(parts.get(0).toLowerCase());

    for (int i = 1; i < parts.size(); i++) {
      String part = parts.get(i).toLowerCase();
      sb.append(Character.toUpperCase(part.charAt(0)));
      if (part.length() > 1) {
        sb.append(part.substring(1));
      }
    }

    return sb.toString();
  }

  static String pascalCase(String s) {
    return capitalize(camelCase(s));
  }

  static String toLanguageClassName(
      Language language, @Nullable GenerationContext generationContext) {
    Objects.requireNonNull(language, "language should not be null");
    Objects.requireNonNull(language.getName(), "language.getName() should not be null");
    String[] parts = language.getName().split("\\.");
    String s = capitalize(parts[parts.length - 1]) + "Language";
    if (generationContext != null && generationContext.hasOverridenName(language)) {
      return generationContext.getOverriddenName(language);
    }
    if (generationContext != null && generationContext.ambiguousLanguages().contains(language)) {
      s = s + "V" + language.getVersion();
    }
    s = s.replaceAll(" ", "");
    s = s.replaceAll("\\.", "_");
    return s;
  }
}
