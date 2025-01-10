package io.lionweb.lioncore.java.serialization;

import static io.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.data.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** Specific tests of JsonSerialization using the LionCore example. */
public class SerializationOfLionCoreTest extends SerializationTest {

  @Test
  public void serializeLionCoreToSerializedChunkV2023() {
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    SerializedChunk serializedChunk =
        jsonSerialization.serializeTreeToSerializationBlock(
            LionCore.getInstance(LionWebVersion.v2023_1));

    assertEquals("2023.1", serializedChunk.getSerializationFormatVersion());

    assertEquals(2, serializedChunk.getLanguages().size());
    Assert.assertEquals(
        new UsedLanguage("LionCore-M3", "2023.1"),
        serializedChunk.getLanguages().iterator().next());

    SerializedClassifierInstance LionCore_M3 =
        serializedChunk.getClassifierInstances().stream()
            .filter(n -> "-id-LionCore-M3".equals(n.getID()))
            .findFirst()
            .get();
    assertEquals("-id-LionCore-M3", LionCore_M3.getID());
    assertEquals(new MetaPointer("LionCore-M3", "2023.1", "Language"), LionCore_M3.getClassifier());
    assertEquals(
        Arrays.asList(
            new SerializedPropertyValue(
                new MetaPointer("LionCore-M3", "2023.1", "Language-version"), "2023.1"),
            new SerializedPropertyValue(
                new MetaPointer("LionCore-M3", "2023.1", "IKeyed-key"), "LionCore-M3"),
            new SerializedPropertyValue(
                new MetaPointer("LionCore-builtins", "2023.1", "LionCore-builtins-INamed-name"),
                "LionCore_M3")),
        LionCore_M3.getProperties());
    assertEquals(
        Arrays.asList(
            new SerializedContainmentValue(
                new MetaPointer("LionCore-M3", "2023.1", "Language-entities"),
                Arrays.asList(
                    "-id-Annotation",
                    "-id-Concept",
                    "-id-Interface",
                    "-id-Containment",
                    "-id-DataType",
                    "-id-Enumeration",
                    "-id-EnumerationLiteral",
                    "-id-Feature",
                    "-id-Classifier",
                    "-id-Link",
                    "-id-Language",
                    "-id-LanguageEntity",
                    "-id-IKeyed",
                    "-id-PrimitiveType",
                    "-id-Property",
                    "-id-Reference"))),
        LionCore_M3.getContainments());
    assertEquals(
        Arrays.asList(
            new SerializedReferenceValue(
                new MetaPointer("LionCore-M3", "2023.1", "Language-dependsOn"),
                Collections.emptyList())),
        LionCore_M3.getReferences());

    SerializedClassifierInstance LionCore_M3_Interface_extends =
        serializedChunk.getClassifierInstances().stream()
            .filter(n -> "-id-Interface-extends".equals(n.getID()))
            .findFirst()
            .get();
  }

  @Test
  public void serializeLionCoreToSerializedChunkV2024() {
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    SerializedChunk serializedChunk =
        jsonSerialization.serializeTreeToSerializationBlock(LionCore.getInstance());

    assertEquals("2024.1", serializedChunk.getSerializationFormatVersion());

    assertEquals(2, serializedChunk.getLanguages().size());
    Assert.assertEquals(
        new UsedLanguage("LionCore-M3", "2024.1"),
        serializedChunk.getLanguages().iterator().next());

    SerializedClassifierInstance LionCore_M3 =
        serializedChunk.getClassifierInstances().stream()
            .filter(n -> "-id-LionCore-M3-2024-1".equals(n.getID()))
            .findFirst()
            .get();
    assertEquals("-id-LionCore-M3-2024-1", LionCore_M3.getID());
    assertEquals(new MetaPointer("LionCore-M3", "2024.1", "Language"), LionCore_M3.getClassifier());
    assertEquals(
        Arrays.asList(
            new SerializedPropertyValue(
                new MetaPointer("LionCore-M3", "2024.1", "Language-version"), "2024.1"),
            new SerializedPropertyValue(
                new MetaPointer("LionCore-M3", "2024.1", "IKeyed-key"), "LionCore-M3"),
            new SerializedPropertyValue(
                new MetaPointer("LionCore-builtins", "2024.1", "LionCore-builtins-INamed-name"),
                "LionCore_M3")),
        LionCore_M3.getProperties());
    assertEquals(
        Arrays.asList(
            new SerializedContainmentValue(
                new MetaPointer("LionCore-M3", "2024.1", "Language-entities"),
                Arrays.asList(
                    "-id-Annotation-2024-1",
                    "-id-Concept-2024-1",
                    "-id-Interface-2024-1",
                    "-id-Containment-2024-1",
                    "-id-DataType-2024-1",
                    "-id-Enumeration-2024-1",
                    "-id-EnumerationLiteral-2024-1",
                    "-id-Feature-2024-1",
                    "-id-Field-2024-1",
                    "-id-Classifier-2024-1",
                    "-id-Link-2024-1",
                    "-id-Language-2024-1",
                    "-id-LanguageEntity-2024-1",
                    "-id-IKeyed-2024-1",
                    "-id-PrimitiveType-2024-1",
                    "-id-Property-2024-1",
                    "-id-Reference-2024-1",
                    "-id-StructuredDataType-2024-1"))),
        LionCore_M3.getContainments());
    assertEquals(
        Arrays.asList(
            new SerializedReferenceValue(
                new MetaPointer("LionCore-M3", "2024.1", "Language-dependsOn"),
                Collections.emptyList())),
        LionCore_M3.getReferences());

    SerializedClassifierInstance LionCore_M3_Interface_extends =
        serializedChunk.getClassifierInstances().stream()
            .filter(n -> "-id-Interface-extends-2024-1".equals(n.getID()))
            .findFirst()
            .get();
  }

  @Test
  public void serializeLionCoreToJSON() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement serializedElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    JsonElement reserialized =
        jsonSerialization.serializeTreeToJsonElement(LionCore.getInstance(LionWebVersion.v2023_1));
    assertEquivalentLionWebJson(
        serializedElement.getAsJsonObject(), reserialized.getAsJsonObject());
  }

  @Test
  public void deserializeLionCoreToSerializedChunk() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    SerializedChunk serializedChunk =
        new LowLevelJsonSerialization().deserializeSerializationBlock(jsonElement);
    List<SerializedClassifierInstance> deserializedSerializedClassifierInstanceData =
        serializedChunk.getClassifierInstances();

    SerializedClassifierInstance lioncore = serializedChunk.getInstanceByID("-id-LionCore-M3");
    assertEquals(
        MetaPointer.from(LionCore.getLanguage(LionWebVersion.v2023_1)), lioncore.getClassifier());
    assertEquals("-id-LionCore-M3", lioncore.getID());
    assertEquals("LionCore_M3", lioncore.getPropertyValue("LionCore-builtins-INamed-name"));
    assertEquals(16, lioncore.getChildren().size());
    assertNull(lioncore.getParentNodeID());
  }

  @Test
  public void deserializeLionCoreToConcreteClasses() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(jsonElement);

    Language lioncore = (Language) deserializedNodes.get(0);
    assertEquals(LionCore.getLanguage(LionWebVersion.v2023_1), lioncore.getClassifier());
    assertEquals("-id-LionCore-M3", lioncore.getID());
    assertEquals("LionCore_M3", lioncore.getName());
    assertEquals(16, ClassifierInstanceUtils.getChildren(lioncore).size());
    assertNull(lioncore.getParent());
  }

  @Test
  public void deserializeLionCoreToDynamicNodes() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization =
        SerializationProvider.getBasicJsonSerialization(LionWebVersion.v2023_1);
    jsonSerialization.getInstanceResolver().addAll(LionCore.getInstance().thisAndAllDescendants());
    jsonSerialization
        .getInstanceResolver()
        .addAll(LionCoreBuiltins.getInstance(LionWebVersion.v2023_1).thisAndAllDescendants());
    jsonSerialization
        .getClassifierResolver()
        .registerLanguage(LionCore.getInstance(LionWebVersion.v2023_1));
    jsonSerialization.getInstantiator().enableDynamicNodes();
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerLionBuiltinsPrimitiveSerializersAndDeserializers(LionWebVersion.v2023_1);
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(jsonElement);

    DynamicNode lioncore = (DynamicNode) deserializedNodes.get(0);
    assertEquals(LionCore.getLanguage(LionWebVersion.v2023_1), lioncore.getClassifier());
    assertEquals("-id-LionCore-M3", lioncore.getID());
    assertEquals("LionCore_M3", ClassifierInstanceUtils.getPropertyValueByName(lioncore, "name"));
    assertEquals(16, ClassifierInstanceUtils.getChildren(lioncore).size());
    assertNull(lioncore.getParent());
  }

  @Test(expected = RuntimeException.class)
  public void deserializeLionCoreFailsWithoutRegisteringTheClassesOrEnablingDynamicNodes() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = SerializationProvider.getBasicJsonSerialization();
    jsonSerialization.getClassifierResolver().registerLanguage(LionCore.getInstance());
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerLionBuiltinsPrimitiveSerializersAndDeserializers(LionWebVersion.currentVersion);
    jsonSerialization.deserializeToNodes(jsonElement);
  }
}
