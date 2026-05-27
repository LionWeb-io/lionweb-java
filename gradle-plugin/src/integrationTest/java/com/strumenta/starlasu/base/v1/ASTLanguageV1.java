package com.strumenta.starlasu.base.v1;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Language;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Stub language singleton for the StarLasu AST language.
 *
 * <p>The db2sql language generator config maps "com.strumenta.StarLasu" ->
 * "com.strumenta.starlasu.base.v1.ASTLanguageV1". The generated Db2sqlLanguage class calls
 * ASTLanguageV1.getLanguage() when it resolves cross-language concept references (e.g.
 * DB2SQLStatement.setExtendedConcept(...)).
 *
 * <p>Loads the real StarLasu language from ast.language.v1.json at first use.
 */
public class ASTLanguageV1 {

  private static volatile Language language;

  public static Language getLanguage() {
    if (language == null) {
      synchronized (ASTLanguageV1.class) {
        if (language == null) {
          language = loadLanguage();
        }
      }
    }
    return language;
  }

  private static Language loadLanguage() {
    try {
      String json;
      try (InputStream in =
          ASTLanguageV1.class.getResourceAsStream("/db2sql/ast.language.v1.json")) {
        if (in == null) {
          throw new IllegalStateException("ast.language.v1.json not found on classpath");
        }
        json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      }
      SerializationChunk chunk =
          new LowLevelJsonSerialization().deserializeSerializationBlock(json);
      JsonSerialization ser =
          SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
      Language lang =
          (Language)
              ser.deserializeSerializationChunk(chunk).stream()
                  .filter(n -> n.getParent() == null)
                  .findFirst()
                  .orElseThrow(() -> new IllegalStateException("No root node in StarLasu chunk"));
      ser.registerLanguage(lang);
      return lang;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load StarLasu (ast.language.v1.json)", e);
    }
  }
}
