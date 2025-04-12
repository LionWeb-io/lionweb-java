package io.lionweb.serialization.extensions.library;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class LibraryLanguage {

  public static final Language LANGUAGE;
  public static final Concept LIBRARY;
  public static final Concept BOOK;
  public static final Concept WRITER;

  public static final Concept GUIDE_BOOK_WRITER;

  static {
    InputStream inputStream =
        LibraryLanguage.class.getResourceAsStream("/serialization/library-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(jsonElement);
    LANGUAGE =
        deserializedNodes.stream()
            .filter(e -> e instanceof Language)
            .map(e -> (Language) e)
            .findFirst()
            .get();
    LIBRARY = LANGUAGE.getConceptByName("Library");
    BOOK = LANGUAGE.getConceptByName("Book");
    WRITER = LANGUAGE.getConceptByName("Writer");
    GUIDE_BOOK_WRITER = LANGUAGE.getConceptByName("GuideBookWriter");

    LIBRARY
        .allFeatures()
        .forEach(
            f ->
                Objects.requireNonNull(
                    f.getKey(),
                    "Feature " + f + " in " + f.getContainer() + " should not have a null key"));
  }
}
