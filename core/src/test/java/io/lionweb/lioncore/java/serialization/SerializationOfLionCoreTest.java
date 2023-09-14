package io.lionweb.lioncore.java.serialization;

import static io.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;
import static org.junit.Assert.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
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
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    SerializedChunk serializedChunk =
        jsonSerialization.serializeTreeToSerializationBlock(LionCore.getInstance());

    assertEquals("1", serializedChunk.getSerializationFormatVersion());

    assertEquals(1, serializedChunk.getLanguages().size());
    Assert.assertEquals(
        new UsedLanguage("LIonCore-M3", "1"), serializedChunk.getLanguages().get(0));

    SerializedClassifierInstance LIonCore_M3 =
        serializedChunk.getClassifierInstances().stream()
            .filter(n -> n.getID().equals("-id-LIonCore-M3"))
            .findFirst()
            .get();
    assertEquals("-id-LIonCore-M3", LIonCore_M3.getID());
    assertEquals(new MetaPointer("LIonCore-M3", "1", "Language"), LIonCore_M3.getClassifier());
    assertEquals(
        Arrays.asList(
            new SerializedPropertyValue(
                new MetaPointer("LIonCore-M3", "1", "Language-version"), "1"),
            new SerializedPropertyValue(
                new MetaPointer("LIonCore-M3", "1", "IKeyed-key"), "LIonCore-M3"),
            new SerializedPropertyValue(
                new MetaPointer("LIonCore-builtins", "1", "LIonCore-builtins-INamed-name"),
                "LIonCore.M3")),
        LIonCore_M3.getProperties());
    assertEquals(
        Arrays.asList(
            new SerializedContainmentValue(
                new MetaPointer("LIonCore-M3", "1", "Language-entities"),
                Arrays.asList(
                    "-id-Annotation",
                    "-id-Concept",
                    "-id-ConceptInterface",
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
        LIonCore_M3.getContainments());
    assertEquals(
        Arrays.asList(
            new SerializedReferenceValue(
                new MetaPointer("LIonCore-M3", "1", "Language-dependsOn"),
                Collections.emptyList())),
        LIonCore_M3.getReferences());

    SerializedClassifierInstance LIonCore_M3_ConceptInterface_extends =
        serializedChunk.getClassifierInstances().stream()
            .filter(n -> n.getID().equals("-id-ConceptInterface-extends"))
            .findFirst()
            .get();
  }

  @Test
  public void serializeLionCoreToJSON() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement serializedElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    JsonElement reserialized = jsonSerialization.serializeTreeToJsonElement(LionCore.getInstance());
    assertEquivalentLionWebJson(
        serializedElement.getAsJsonObject(), reserialized.getAsJsonObject());
  }

  @Test
  public void unserializeLionCoreToSerializedChunk() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    SerializedChunk serializedChunk =
        new LowLevelJsonSerialization().unserializeSerializationBlock(jsonElement);
    List<SerializedClassifierInstance> unserializedSerializedClassifierInstanceData = serializedChunk.getClassifierInstances();

    SerializedClassifierInstance lioncore = serializedChunk.getNodeByID("-id-LIonCore-M3");
    assertEquals(MetaPointer.from(LionCore.getLanguage()), lioncore.getClassifier());
    assertEquals("-id-LIonCore-M3", lioncore.getID());
    assertEquals("LIonCore.M3", lioncore.getPropertyValue("LIonCore-builtins-INamed-name"));
    assertEquals(16, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParentNodeID());
  }

  @Test
  public void unserializeLionCoreToConcreteClasses() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNodes(jsonElement);

    Language lioncore = (Language) unserializedNodes.get(0);
    assertEquals(LionCore.getLanguage(), lioncore.getConcept());
    assertEquals("-id-LIonCore-M3", lioncore.getID());
    assertEquals("LIonCore.M3", lioncore.getName());
    assertEquals(16, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParent());
  }

  @Test
  public void unserializeLionCoreToDynamicNodes() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getBasicSerialization();
    jsonSerialization.getNodeResolver().addAll(LionCore.getInstance().thisAndAllDescendants());
    jsonSerialization
        .getNodeResolver()
        .addAll(LionCoreBuiltins.getInstance().thisAndAllDescendants());
    jsonSerialization.getClassifierResolver().registerLanguage(LionCore.getInstance());
    jsonSerialization.getNodeInstantiator().enableDynamicNodes();
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerLionBuiltinsPrimitiveSerializersAndUnserializers();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNodes(jsonElement);

    DynamicNode lioncore = (DynamicNode) unserializedNodes.get(0);
    assertEquals(LionCore.getLanguage(), lioncore.getConcept());
    assertEquals("-id-LIonCore-M3", lioncore.getID());
    assertEquals("LIonCore.M3", lioncore.getPropertyValueByName("name"));
    assertEquals(16, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParent());
  }

  @Test(expected = RuntimeException.class)
  public void unserializeLionCoreFailsWithoutRegisteringTheClassesOrEnablingDynamicNodes() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getBasicSerialization();
    jsonSerialization.getClassifierResolver().registerLanguage(LionCore.getInstance());
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerLionBuiltinsPrimitiveSerializersAndUnserializers();
    jsonSerialization.unserializeToNodes(jsonElement);
  }
}
