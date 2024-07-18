package io.lionweb.lioncore.java.serialization;

import static io.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
  public void serializeLionCoreToSerializedChunk() {
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    SerializedChunk serializedChunk =
        jsonSerialization.serializeTreeToSerializationBlock(LionCore.getInstance());

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
  public void serializeLionCoreToJSON() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement serializedElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    JsonElement reserialized = jsonSerialization.serializeTreeToJsonElement(LionCore.getInstance());
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
    assertEquals(MetaPointer.from(LionCore.getLanguage()), lioncore.getClassifier());
    assertEquals("-id-LionCore-M3", lioncore.getID());
    assertEquals("LionCore_M3", lioncore.getPropertyValue("LionCore-builtins-INamed-name"));
    assertEquals(16, lioncore.getChildren().size());
    assertNull(lioncore.getParentNodeID());
  }

  @Test
  public void deserializeLionCoreToConcreteClasses() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(jsonElement);

    Language lioncore = (Language) deserializedNodes.get(0);
    assertEquals(LionCore.getLanguage(), lioncore.getClassifier());
    assertEquals("-id-LionCore-M3", lioncore.getID());
    assertEquals("LionCore_M3", lioncore.getName());
    assertEquals(16, ClassifierInstanceUtils.getChildren(lioncore).size());
    assertNull(lioncore.getParent());
  }

  @Test
  public void deserializeLionCoreToDynamicNodes() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = SerializationProvider.getBasicJsonSerialization();
    jsonSerialization.getInstanceResolver().addAll(LionCore.getInstance().thisAndAllDescendants());
    jsonSerialization
        .getInstanceResolver()
        .addAll(LionCoreBuiltins.getInstance().thisAndAllDescendants());
    jsonSerialization.getClassifierResolver().registerLanguage(LionCore.getInstance());
    jsonSerialization.getInstantiator().enableDynamicNodes();
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerLionBuiltinsPrimitiveSerializersAndDeserializers();
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(jsonElement);

    DynamicNode lioncore = (DynamicNode) deserializedNodes.get(0);
    assertEquals(LionCore.getLanguage(), lioncore.getClassifier());
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
        .registerLionBuiltinsPrimitiveSerializersAndDeserializers();
    jsonSerialization.deserializeToNodes(jsonElement);
  }
}
