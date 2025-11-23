package io.lionweb.gradleplugin.generators;

import static io.lionweb.gradleplugin.generators.NamingUtils.camelCase;

import io.lionweb.language.*;
import java.io.File;
import java.util.*;
import javax.annotation.Nonnull;

public abstract class AbstractJavaCodeGenerator {
  protected final @Nonnull File destinationDir;

  /**
   * Constructs an AbstractJavaCodeGenerator with a specified destination directory.
   *
   * @param destinationDir the directory where the generated code will be stored; must not be null
   * @throws NullPointerException if the destinationDir is null
   */
  protected AbstractJavaCodeGenerator(@Nonnull File destinationDir) {
    Objects.requireNonNull(destinationDir, "destinationDir should not be null");
    this.destinationDir = destinationDir;
  }

  protected static final List<String> JAVA_KEYWORDS =
      Arrays.asList(
          "abstract",
          "assert",
          "boolean",
          "break",
          "byte",
          "case",
          "catch",
          "char",
          "class",
          "const",
          "continue",
          "default",
          "do",
          "double",
          "else",
          "enum",
          "extends",
          "final",
          "finally",
          "float",
          "for",
          "goto",
          "if",
          "implements",
          "import",
          "instanceof",
          "int",
          "interface",
          "long",
          "native",
          "new",
          "package",
          "private",
          "protected",
          "public",
          "return",
          "short",
          "static",
          "strictfp",
          "super",
          "switch",
          "synchronized",
          "this",
          "throw",
          "throws",
          "transient",
          "try",
          "void",
          "volatile",
          "while");

  protected String toVariableName(String name) {
    String res = camelCase(name);
    if (JAVA_KEYWORDS.contains(res)) {
      return "_" + res;
    } else {
      return res;
    }
  }
}
