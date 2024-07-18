package io.lionweb.lioncore.java.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.junit.Test;

/** Specific tests of JsonSerialization using the Library example. */
public class SerializationOfLibraryTest extends SerializationTest {

  @Test
  public void deserializeLibraryToConcreteClasses() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(jsonElement);

    Concept library = conceptByID(deserializedNodes, "library-Library");
    Property libraryName = library.getPropertyByName("name");
    assertNotNull(libraryName.getKey());

    Node book =
        deserializedNodes.stream().filter(n -> n.getID().equals("library-Book")).findFirst().get();
    assertEquals("Book", ClassifierInstanceUtils.getPropertyValueByName(book, "name"));
    assertEquals("library-Book", ClassifierInstanceUtils.getPropertyValueByName(book, "key"));

    Concept guidedBookWriter =
        (Concept)
            deserializedNodes.stream()
                .filter(n -> n.getID().equals("library-GuideBookWriter"))
                .findFirst()
                .get();
    assertEquals(
        "GuideBookWriter",
        ClassifierInstanceUtils.getPropertyValueByName(guidedBookWriter, "name"));
    assertNotNull(guidedBookWriter.getExtendedConcept());
  }

  @Test
  public void reserializeLibrary() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(jsonElement);
    JsonElement reserialized =
        jsonSerialization.serializeTreeToJsonElement(deserializedNodes.get(0));
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(
        jsonElement.getAsJsonObject(), reserialized.getAsJsonObject());
  }

  @Test
  public void serializeLibraryInstance() {
    Library library = new Library("lib-1", "Language Engineering Library");
    Writer mv = new Writer("mv", "Markus Völter");
    Writer mb = new Writer("mb", "Meinte Boersma");
    Book de = new Book("de", "DSL Engineering", mv).setPages(558);
    Book bfd = new Book("bfd", "Business-Friendly DSLs", mb).setPages(517);
    library.addBook(de);
    library.addBook(bfd);

    // The library MM is not using the standard primitive types but its own, so we need to specify
    // how to serialize
    // those values
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerSerializer(
            "INhBvWyXvxwNsePuX0rdNGB_J9hi85cTb1Q0APXCyJ0",
            (PrimitiveValuesSerialization.PrimitiveSerializer<String>) value -> value);
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerSerializer(
            "gVp8_QSmXE2k4pd-sQZgjYMoW95SLLaVIH4yMYqqbt4",
            (PrimitiveValuesSerialization.PrimitiveSerializer<Integer>) value -> value.toString());
    JsonObject jsonSerialized =
        jsonSerialization.serializeTreeToJsonElement(library).getAsJsonObject();
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/langeng-library.json");
    JsonObject jsonRead =
        JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(jsonRead, jsonSerialized);
  }

  @Test(expected = IllegalStateException.class)
  public void deserializeLanguageWithDuplicateIDs() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-language-with-duplicate.json");
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    jsonSerialization.deserializeToNodes(inputStream);
  }
}
