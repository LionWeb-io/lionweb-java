package io.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.Node;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class LibraryLanguage {

  public static Language LIBRARY_MM;
  public static Concept LIBRARY;
  public static Concept BOOK;
  public static Concept WRITER;

  public static Concept GUIDE_BOOK_WRITER;

  static {
    InputStream inputStream =
        LibraryLanguage.class.getResourceAsStream("/serialization/library-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNodes(jsonElement);
    LIBRARY_MM =
        unserializedNodes.stream()
            .filter(e -> e instanceof Language)
            .map(e -> (Language) e)
            .findFirst()
            .get();
    LIBRARY = LIBRARY_MM.getConceptByName("Library");
    BOOK = LIBRARY_MM.getConceptByName("Book");
    WRITER = LIBRARY_MM.getConceptByName("Writer");
    GUIDE_BOOK_WRITER = LIBRARY_MM.getConceptByName("GuideBookWriter");

    LIBRARY
        .allFeatures()
        .forEach(
            f ->
                Objects.requireNonNull(
                    f.getKey(),
                    "Feature " + f + " in " + f.getContainer() + " should not have a null key"));
  }
}
