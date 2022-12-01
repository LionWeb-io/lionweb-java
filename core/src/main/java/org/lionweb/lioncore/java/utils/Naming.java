package org.lionweb.lioncore.java.utils;

import java.util.regex.Pattern;

public class Naming {
    public static void validateQualifiedName(String qualifiedName) {
        if (!Pattern.matches("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*", qualifiedName)) {
            throw new InvalidName("qualified name", qualifiedName);
        }
    }

    public static void validateSimpleName(String simpleName) {
        if (!Pattern.matches("[a-zA-Z][a-zA-Z0-9_]*", simpleName)) {
            throw new InvalidName("simple name", simpleName);
        }
    }
}
