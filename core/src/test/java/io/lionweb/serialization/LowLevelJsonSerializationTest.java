package io.lionweb.serialization;

import static io.lionweb.serialization.SerializationProvider.getStandardJsonSerialization;
import static io.lionweb.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.LionWebVersion;
import io.lionweb.language.Annotation;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.Node;
import io.lionweb.model.impl.DynamicAnnotationInstance;
import io.lionweb.model.impl.DynamicNode;
import io.lionweb.serialization.data.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class LowLevelJsonSerializationTest extends SerializationTest {

  @Test
  public void deserializeLionCoreToSerializedNodes() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
    SerializationChunk serializationChunk =
        jsonSerialization.deserializeSerializationBlock(jsonElement);
    List<SerializedClassifierInstance> deserializedSerializedClassifierInstanceData =
        serializationChunk.getClassifierInstances();

    SerializedClassifierInstance lioncore = deserializedSerializedClassifierInstanceData.get(0);
    assertEquals(MetaPointer.get("LionCore-M3", "2023.1", "Language"), lioncore.getClassifier());
    assertEquals("-id-LionCore-M3", lioncore.getID());
    assertEquals("LionCore_M3", lioncore.getPropertyValue("LionCore-builtins-INamed-name"));
    assertEquals(
        "LionCore_M3",
        lioncore.getPropertyValue(
            MetaPointer.from(
                LionCoreBuiltins.getINamed(LionWebVersion.v2023_1).getPropertyByName("name"))));
    assertEquals(16, lioncore.getChildren().size());
    assertNull(lioncore.getParentNodeID());
  }

  @Test
  public void deserializeLibraryLanguageToSerializedNodes() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/library-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
    SerializationChunk serializationChunk =
        jsonSerialization.deserializeSerializationBlock(jsonElement);
    SerializedClassifierInstance book = serializationChunk.getInstanceByID("library-Book");
    assertEquals("Book", book.getPropertyValue("LionCore-builtins-INamed-name"));

    SerializedClassifierInstance guidedBookWriter =
        serializationChunk.getInstanceByID("library-GuideBookWriter");
    assertEquals(
        "GuideBookWriter", guidedBookWriter.getPropertyValue("LionCore-builtins-INamed-name"));
    assertEquals(
        Arrays.asList(new SerializedReferenceValue.Entry("library-Writer", "Writer")),
        guidedBookWriter.getReferenceValues("Concept-extends"));
    assertEquals(
        Arrays.asList(new SerializedReferenceValue.Entry("library-Writer", "Writer")),
        guidedBookWriter.getReferenceValues(
            MetaPointer.from(
                LionCore.getConcept(LionWebVersion.v2023_1).getReferenceByName("extends"))));
    assertEquals(
        Arrays.asList("library-GuideBookWriter-countries"),
        guidedBookWriter.getContainmentValues(
            MetaPointer.from(
                LionCore.getConcept(LionWebVersion.v2023_1).getContainmentByName("features"))));
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

  @Test
  public void serializeAnnotations() {
    Language l = new Language("l", "l", "l", "1");
    Annotation a1 = new Annotation(l, "a1", "a1", "a1");
    Annotation a2 = new Annotation(l, "a2", "a2", "a2");
    Concept c = new Concept(l, "c", "c", "c");

    DynamicNode n1 = new DynamicNode("n1", c);
    AnnotationInstance a1_1 = new DynamicAnnotationInstance("a1_1", a1, n1);
    AnnotationInstance a1_2 = new DynamicAnnotationInstance("a1_2", a1, n1);
    AnnotationInstance a2_3 = new DynamicAnnotationInstance("a2_3", a2, n1);

    JsonSerialization hjs = getStandardJsonSerialization();
    hjs.enableDynamicNodes();

    JsonElement je = hjs.serializeNodesToJsonElement(n1);
    List<Node> deserializedNodes = hjs.deserializeToNodes(je);
    assertEquals(1, deserializedNodes.size());
    assertInstancesAreEquals(n1, deserializedNodes.get(0));
  }

  @Test(expected = RuntimeException.class)
  public void unexepectedProperty() {
    String json =
        "{\n"
            + "  \"serializationFormatVersion\": \"1\",\n"
            + "  \"languages\": [],\n"
            + "  \"nodes\": [],\n"
            + "  \"info\": \"should not be here\"\n"
            + "}";
    LowLevelJsonSerialization lljs = new LowLevelJsonSerialization();
    lljs.deserializeSerializationBlock(json);
  }

  private void assertTheFileIsReserializedFromLowLevelCorrectly(String filePath) {
    InputStream inputStream = this.getClass().getResourceAsStream(filePath);
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
    SerializationChunk serializationChunk =
        jsonSerialization.deserializeSerializationBlock(jsonElement);
    JsonElement reserialized = jsonSerialization.serializeToJsonElement(serializationChunk);
    assertEquivalentLionWebJson(jsonElement.getAsJsonObject(), reserialized.getAsJsonObject());
  }
}
