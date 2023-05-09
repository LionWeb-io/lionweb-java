package io.lionweb.lioncore.java.serialization;

import static io.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;
import static org.junit.Assert.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.java.serialization.data.SerializedNode;
import io.lionweb.lioncore.java.serialization.data.SerializedReferenceValue;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class LowLevelJsonSerializationTest {

  @Test
  public void unserializeLionCoreToSerializedNodes() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
    SerializedChunk serializedChunk = jsonSerialization.unserializeSerializationBlock(jsonElement);
    List<SerializedNode> unserializedSerializedNodeData = serializedChunk.getNodes();

    SerializedNode lioncore = unserializedSerializedNodeData.get(0);
    assertEquals(new MetaPointer("LIonCore_M3", "1", "Language"), lioncore.getConcept());
    assertEquals("LIonCore_M3", lioncore.getID());
    assertEquals("LIonCore.M3", lioncore.getPropertyValue("Language_name"));
    assertEquals(17, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParentNodeID());

    SerializedNode namespacedEntity =
        unserializedSerializedNodeData.stream()
            .filter(e -> e.getID().equals("LIonCore_M3_NamespacedEntity"))
            .findFirst()
            .get();
    assertEquals(new MetaPointer("LIonCore_M3", "1", "Concept"), namespacedEntity.getConcept());
    assertEquals("LIonCore_M3_NamespacedEntity", namespacedEntity.getID());
    assertEquals("true", namespacedEntity.getPropertyValue("abstract"));
    assertEquals("NamespacedEntity", namespacedEntity.getPropertyValue("NamespacedEntity_name"));
    assertEquals(1, namespacedEntity.getChildren().size());
    assertEquals(lioncore.getID(), namespacedEntity.getParentNodeID());

    SerializedNode name =
        unserializedSerializedNodeData.stream()
            .filter(e -> e.getID().equals("LIonCore_M3_NamespacedEntity_name"))
            .findFirst()
            .get();
    assertEquals(new MetaPointer("LIonCore_M3", "1", "Property"), name.getConcept());
    assertEquals("name", name.getPropertyValue("NamespacedEntity_name"));
    assertEquals("LIonCore_M3_NamespacedEntity", name.getParentNodeID());
    assertEquals(
        Arrays.asList(new SerializedReferenceValue.Entry("LIonCore_M3_String", "String")),
        name.getReferenceValues("Property_type"));
  }

  @Test
  public void unserializeLibraryLanguageToSerializedNodes() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
    SerializedChunk serializedChunk = jsonSerialization.unserializeSerializationBlock(jsonElement);
    SerializedNode book = serializedChunk.getNodeByID("library-Book");
    assertEquals("Book", book.getPropertyValue("NamespacedEntity_name"));

    SerializedNode guidedBookWriter = serializedChunk.getNodeByID("library-GuideBookWriter");
    assertEquals("GuideBookWriter", guidedBookWriter.getPropertyValue("NamespacedEntity_name"));
    assertEquals(
        Arrays.asList(new SerializedReferenceValue.Entry("library-Writer", "Writer")),
        guidedBookWriter.getReferenceValues("Concept_extends"));
  }

  @Test
  public void reserializeLibraryLanguage() {
    assertTheFileIsReserializedFromLowLevelCorrectly("/serialization/library-language.json");
  }

  @Test
  public void reserializeBobsLibrary() {
    assertTheFileIsReserializedFromLowLevelCorrectly("/serialization/bobslibrary.json");
  }

  @Test
  public void reserializeLanguageEngineeringLibrary() {
    assertTheFileIsReserializedFromLowLevelCorrectly("/serialization/langeng-library.json");
  }

  private void assertTheFileIsReserializedFromLowLevelCorrectly(String filePath) {
    InputStream inputStream = this.getClass().getResourceAsStream(filePath);
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
    SerializedChunk serializedChunk = jsonSerialization.unserializeSerializationBlock(jsonElement);
    JsonElement reserialized = jsonSerialization.serializeToJsonElement(serializedChunk);
    assertEquivalentLionWebJson(jsonElement.getAsJsonObject(), reserialized.getAsJsonObject());
  }
}
