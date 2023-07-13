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
    assertEquals(new MetaPointer("LIonCore-M3", "1", "Language"), lioncore.getConcept());
    assertEquals("-id-LIonCore-M3", lioncore.getID());
    assertEquals("LIonCore.M3", lioncore.getPropertyValue("LIonCore-builtins-INamed-name"));
    assertEquals(15, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParentNodeID());
  }

  @Test
  public void unserializeLibraryLanguageToSerializedNodes() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
    SerializedChunk serializedChunk = jsonSerialization.unserializeSerializationBlock(jsonElement);
    SerializedNode book = serializedChunk.getNodeByID("library-Book");
    assertEquals("Book", book.getPropertyValue("LIonCore-builtins-INamed-name"));

    SerializedNode guidedBookWriter = serializedChunk.getNodeByID("library-GuideBookWriter");
    assertEquals(
        "GuideBookWriter", guidedBookWriter.getPropertyValue("LIonCore-builtins-INamed-name"));
    assertEquals(
        Arrays.asList(new SerializedReferenceValue.Entry("library-Writer", "Writer")),
        guidedBookWriter.getReferenceValues("Concept-extends"));
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
