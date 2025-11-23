package io.lionweb.gradleplugin.generators;

import io.lionweb.language.Language;

import javax.annotation.Nullable;
import java.util.Objects;

public class NamingUtils {

    protected static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    protected static String camelCase(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        String[] parts = s.trim().split("[^A-Za-z0-9]+");
        if (parts.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(parts[0].toLowerCase());

        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            sb.append(parts[i].substring(0, 1).toUpperCase());
            if (parts[i].length() > 1) {
                sb.append(parts[i].substring(1).toLowerCase());
            }
        }

        return sb.toString();
    }

    protected static String pascalCase(String s) {
        return capitalize(camelCase(s));
    }

    protected static String toLanguageClassName(
            Language language, @Nullable GenerationContext generationContext) {
        Objects.requireNonNull(language, "language should not be null");
        Objects.requireNonNull(language.getName(), "language.getName() should not be null");
        String[] parts = language.getName().split("\\.");
        String s = capitalize(parts[parts.length - 1]) + "Language";
        if (generationContext != null && generationContext.ambiguousLanguages().contains(language)) {
            s = s + "V" + language.getVersion();
        }
        s = s.replaceAll(" ", "");
        s = s.replaceAll("\\.", "_");
        return s;
    }
}
