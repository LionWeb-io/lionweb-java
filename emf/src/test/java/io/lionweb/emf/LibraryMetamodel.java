package io.lionweb.emf;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.LionWebVersion;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.model.Node;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class LibraryMetamodel {

  public static final Language LIBRARY_LANG;
  public static final Concept LIBRARY;
  public static final Concept BOOK;
  public static final Concept WRITER;

  public static final Concept GUIDE_BOOK_WRITER;

  static {
    InputStream inputStream = LibraryMetamodel.class.getResourceAsStream("/library-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(jsonElement);
    LIBRARY_LANG =
        deserializedNodes.stream()
            .filter(e -> e instanceof Language)
            .map(e -> (Language) e)
            .findFirst()
            .get();
    LIBRARY = LIBRARY_LANG.getConceptByName("Library");
    BOOK = LIBRARY_LANG.getConceptByName("Book");
    WRITER = LIBRARY_LANG.getConceptByName("Writer");
    GUIDE_BOOK_WRITER = LIBRARY_LANG.getConceptByName("GuideBookWriter");

    LIBRARY
        .allFeatures()
        .forEach(
            f ->
                Objects.requireNonNull(
                    f.getKey(),
                    "Feature " + f + " in " + f.getContainer() + " should not have a null key"));
  }
}
