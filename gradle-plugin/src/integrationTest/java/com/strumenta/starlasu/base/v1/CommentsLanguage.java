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
 * Stub language singleton for the StarLasu comments language.
 *
 * <p>The db2sql generator config maps "com.strumenta.starlasu.comments" ->
 * "com.strumenta.starlasu.base.v1.CommentsLanguage".
 *
 * <p>Loads from comments.language.v1.json at first use.
 */
public class CommentsLanguage {

  private static volatile Language language;

  public static Language getLanguage() {
    if (language == null) {
      synchronized (CommentsLanguage.class) {
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
          CommentsLanguage.class.getResourceAsStream("/db2sql/comments.language.v1.json")) {
        if (in == null) {
          throw new IllegalStateException("comments.language.v1.json not found on classpath");
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
                  .orElseThrow(() -> new IllegalStateException("No root node in comments chunk"));
      ser.registerLanguage(lang);
      return lang;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load comments language (comments.language.v1.json)", e);
    }
  }
}
