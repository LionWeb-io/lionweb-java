package org.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.Metamodel;
import org.lionweb.lioncore.java.model.Node;

public class LibraryMetamodel {

  public static Metamodel LIBRARY_MM;
  public static Concept LIBRARY;
  public static Concept BOOK;
  public static Concept WRITER;

  public static Concept GUIDE_BOOK_WRITER;

  static {
    InputStream inputStream =
        LibraryMetamodel.class.getResourceAsStream("/serialization/library-metamodel.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNode(jsonElement);
    LIBRARY_MM =
        unserializedNodes.stream()
            .filter(e -> e instanceof Metamodel)
            .map(e -> (Metamodel) e)
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
