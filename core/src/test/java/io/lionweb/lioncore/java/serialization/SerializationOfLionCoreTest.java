package io.lionweb.lioncore.java.serialization;

import static io.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;
import static org.junit.Assert.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.language.Property;
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
        new LanguageKeyVersion("LIonCore-M3", "1"), serializedChunk.getLanguages().get(0));

    SerializedNode LIonCore_M3 =
        serializedChunk.getNodes().stream()
            .filter(n -> n.getID().equals("-id-LIonCore-M3"))
            .findFirst()
            .get();
    assertEquals("-id-LIonCore-M3", LIonCore_M3.getID());
    assertEquals(new MetaPointer("LIonCore-M3", "1", "Language"), LIonCore_M3.getConcept());
    assertEquals(
        Arrays.asList(
            new SerializedPropertyValue(new MetaPointer("LIonCore-M3", "1", "Language-version"), "1"),
            new SerializedPropertyValue(new MetaPointer("LIonCore-M3", "1", "IKeyed-key"), "LIonCore-M3"),
                new SerializedPropertyValue(
                        new MetaPointer("LIonCore-builtins", "1", "LIonCore-builtins-INamed-name"), "LIonCore.M3")),
        LIonCore_M3.getProperties());
    assertEquals(
        Arrays.asList(
            new SerializedContainmentValue(
                new MetaPointer("LIonCore-M3", "1", "Language-entities"),
                Arrays.asList(
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
                new MetaPointer("LIonCore-M3", "1", "Language-dependsOn"), Collections.emptyList())),
        LIonCore_M3.getReferences());

    SerializedNode LIonCore_M3_ConceptInterface_extends =
        serializedChunk.getNodes().stream()
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
    List<SerializedNode> unserializedSerializedNodeData = serializedChunk.getNodes();

    SerializedNode lioncore = serializedChunk.getNodeByID("LIonCore-M3");
    assertEquals(MetaPointer.from(LionCore.getLanguage()), lioncore.getConcept());
    assertEquals("LIonCore-M3", lioncore.getID());
    assertEquals("LIonCore.M3", lioncore.getPropertyValue("Language_name"));
    assertEquals(17, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParentNodeID());

    SerializedNode namespacedEntity = serializedChunk.getNodeByID("LIonCore-M3-NamespacedEntity");
    assertEquals(MetaPointer.from(LionCore.getConcept()), namespacedEntity.getConcept());
    assertEquals("LIonCore-M3-NamespacedEntity", namespacedEntity.getID());
    assertEquals("true", namespacedEntity.getPropertyValue("abstract"));
    assertEquals("NamespacedEntity", namespacedEntity.getPropertyValue("NamespacedEntity_name"));
    assertEquals(2, namespacedEntity.getChildren().size());
    assertEquals(lioncore.getID(), namespacedEntity.getParentNodeID());

    SerializedNode name = serializedChunk.getNodeByID("LIonCore-M3-NamespacedEntity_name");
    assertEquals(MetaPointer.from(LionCore.getProperty()), name.getConcept());
    assertEquals("name", name.getPropertyValue("NamespacedEntity_name"));
    assertEquals("LIonCore-M3-NamespacedEntity", name.getParentNodeID());
    assertEquals(
        Arrays.asList(new SerializedReferenceValue.Entry("LIonCore-M3-String", "String")),
        name.getReferenceValues("Property_type"));
  }

  @Test
  public void unserializeLionCoreToConcreteClasses() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNodes(jsonElement);

    Language lioncore = (Language) unserializedNodes.get(0);
    assertEquals(LionCore.getLanguage(), lioncore.getConcept());
    assertEquals("LIonCore-M3", lioncore.getID());
    assertEquals("LIonCore.M3", lioncore.getName());
    assertEquals(17, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParent());

    Concept namespacedEntity = conceptByID(unserializedNodes, "LIonCore-M3-NamespacedEntity");
    assertEquals(LionCore.getConcept(), namespacedEntity.getConcept());
    assertEquals("LIonCore-M3-NamespacedEntity", namespacedEntity.getID());
    assertEquals(true, namespacedEntity.isAbstract());
    assertEquals("NamespacedEntity", namespacedEntity.getName());
    assertEquals(2, namespacedEntity.getChildren().size());
    assertEquals(lioncore, namespacedEntity.getParent());

    Property name = propertyByID(unserializedNodes, "LIonCore-M3-NamespacedEntity_name");
    assertEquals(LionCore.getProperty(), name.getConcept());
    assertEquals("name", name.getName());
    assertEquals("LIonCore-M3-NamespacedEntity", name.getParent().getID());
    assertEquals("LIonCore-M3-String", name.getType().getID());
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
    jsonSerialization.getConceptResolver().registerLanguage(LionCore.getInstance());
    jsonSerialization.getNodeInstantiator().enableDynamicNodes();
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerLionBuiltinsPrimitiveSerializersAndUnserializers();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNodes(jsonElement);

    DynamicNode lioncore = (DynamicNode) unserializedNodes.get(0);
    assertEquals(LionCore.getLanguage(), lioncore.getConcept());
    assertEquals("LIonCore-M3", lioncore.getID());
    assertEquals("LIonCore.M3", lioncore.getPropertyValueByName("name"));
    assertEquals(17, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParent());

    DynamicNode namespacedEntity =
        dynamicNodeByID(unserializedNodes, "LIonCore-M3-NamespacedEntity");
    assertEquals(LionCore.getConcept(), namespacedEntity.getConcept());
    assertEquals("LIonCore-M3-NamespacedEntity", namespacedEntity.getID());
    assertEquals(true, namespacedEntity.getPropertyValueByName("abstract"));
    assertEquals("NamespacedEntity", namespacedEntity.getPropertyValueByName("name"));
    assertEquals(2, namespacedEntity.getChildren().size());
    assertEquals(lioncore, namespacedEntity.getParent());

    DynamicNode name = dynamicNodeByID(unserializedNodes, "LIonCore-M3-NamespacedEntity_name");
    assertEquals(LionCore.getProperty(), name.getConcept());
    assertEquals("name", name.getPropertyValueByName("name"));
    assertEquals("LIonCore-M3-NamespacedEntity", name.getParent().getID());
  }

  @Test(expected = RuntimeException.class)
  public void unserializeLionCoreFailsWithoutRegisteringTheClassesOrEnablingDynamicNodes() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getBasicSerialization();
    jsonSerialization.getConceptResolver().registerLanguage(LionCore.getInstance());
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerLionBuiltinsPrimitiveSerializersAndUnserializers();
    jsonSerialization.unserializeToNodes(jsonElement);
  }
}
