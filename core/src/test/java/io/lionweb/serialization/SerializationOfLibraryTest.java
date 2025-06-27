package io.lionweb.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.LionWebVersion;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.Property;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Specific tests of JsonSerialization using the Library example. */
public class SerializationOfLibraryTest extends SerializationTest {

  @Test
  public void deserializeLibraryToConcreteClasses() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
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
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
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
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
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
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    jsonSerialization.deserializeToNodes(inputStream);
  }

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void reserializeJsonFile() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(jsonElement);

    Language libraryLanguage = (Language) deserializedNodes.get(0);

    try {
      File outputFile = temporaryFolder.newFile("test-library-lang.json");
      JsonSerialization.saveLanguageToFile(libraryLanguage, outputFile);

      Language loadedLang = jsonSerialization.loadLanguage(outputFile);

      assertEquals(libraryLanguage.getName(), loadedLang.getName());
      assertEquals(libraryLanguage.getKey(), loadedLang.getKey());
      assertEquals(libraryLanguage.getVersion(), loadedLang.getVersion());
      assertEquals(libraryLanguage.getLionWebVersion(), loadedLang.getLionWebVersion());

      Concept book = (Concept) loadedLang.getElementByName("Book");
      assertEquals(book.getLionWebVersion(), libraryLanguage.getLionWebVersion());

      Property bookTitle = (Property) book.getFeatureByName("title");
      assertEquals(bookTitle.getLionWebVersion(), libraryLanguage.getLionWebVersion());
    } catch (IOException ioe) {
      System.err.println(
          "Error creating temporary test file in " + this.getClass().getSimpleName());
    }
  }

  @Test
  public void deserializeExtendedLibrary() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-language.json");
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(inputStream);

    Language libraryLanguage = (Language) deserializedNodes.get(0);

    InputStream inputStream2 =
        this.getClass().getResourceAsStream("/serialization/extendedlibrary-language.json");
    JsonSerialization jsonSerialization2 =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    jsonSerialization2.registerLanguage(libraryLanguage);
    List<Node> deserializedNodes2 = jsonSerialization2.deserializeToNodes(inputStream2);

    Concept localLibrary = conceptByID(deserializedNodes2, "extendedlibrary-LocalLibrary");
    Property libraryName = localLibrary.getPropertyByName("name");
    assertNotNull(libraryName.getKey());
  }
}
