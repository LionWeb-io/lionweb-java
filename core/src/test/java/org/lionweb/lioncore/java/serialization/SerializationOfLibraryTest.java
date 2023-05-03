package org.lionweb.lioncore.java.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.junit.Test;
import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.Property;
import org.lionweb.lioncore.java.model.Node;

/** Specific tests of JsonSerialization using the Library example. */
public class SerializationOfLibraryTest extends SerializationTest {

  @Test
  public void unserializeLibraryToConcreteClasses() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-metamodel.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNodes(jsonElement);

    Concept library = conceptByID(unserializedNodes, "library-Library");
    Property libraryName = library.getPropertyByName("name");
    assertNotNull(libraryName.getKey());

    Node book =
        unserializedNodes.stream().filter(n -> n.getID().equals("library-Book")).findFirst().get();
    assertEquals("Book", book.getPropertyValueByName("name"));
    assertEquals("library-Book", book.getPropertyValueByName("key"));

    Concept guidedBookWriter =
        (Concept)
            unserializedNodes.stream()
                .filter(n -> n.getID().equals("library-GuideBookWriter"))
                .findFirst()
                .get();
    assertEquals("GuideBookWriter", guidedBookWriter.getPropertyValueByName("name"));
    assertNotNull(guidedBookWriter.getExtendedConcept());
  }

  @Test
  public void reserializeLibrary() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-metamodel.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNodes(jsonElement);
    JsonElement reserialized =
        jsonSerialization.serializeTreeToJsonElement(unserializedNodes.get(0));
    assertEquivalentLionWebJson(jsonElement.getAsJsonObject(), reserialized.getAsJsonObject());
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
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
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
    assertEquivalentLionWebJson(jsonRead, jsonSerialized);
  }

  @Test(expected = IllegalStateException.class)
  public void unserializeMetamodelWithDuplicateIDs() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-metamodel-with-duplicate.json");
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    jsonSerialization.unserializeToNodes(inputStream);
  }
}
