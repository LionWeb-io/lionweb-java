package org.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.impl.DynamicNode;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.serialization.data.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;

/**
 * Specific tests of JsonSerialization using the LionCore example.
 */
public class SerializationOfLionCoreTest extends SerializationTest {

    @Test
    public void serializeLionCoreToSerializedChunk() {
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        SerializedChunk serializedChunk = jsonSerialization.serializeTreeToSerializationBlock(LionCore.getInstance());

        assertEquals("1", serializedChunk.getSerializationFormatVersion());

        assertEquals(1, serializedChunk.getMetamodels().size());
        assertEquals(new MetamodelKeyVersion("LIonCore_M3", "1"), serializedChunk.getMetamodels().get(0));

        SerializedNode LIonCore_M3 = serializedChunk.getNodes().stream().filter(n -> n.getID().equals("LIonCore_M3")).findFirst().get();
        assertEquals("LIonCore_M3", LIonCore_M3.getID());
        assertEquals(new MetaPointer("LIonCore_M3", "1", "LIonCore_M3_Metamodel"), LIonCore_M3.getConcept());
        assertEquals(Arrays.asList(
                        new SerializedPropertyValue(new MetaPointer("LIonCore_M3", "1", "LIonCore_M3_Metamodel_name"), "LIonCore.M3"),
                        new SerializedPropertyValue(new MetaPointer("LIonCore_M3", "1", "LIonCore_M3_Metamodel_version"), "1"),
                        new SerializedPropertyValue(new MetaPointer("LIonCore_M3", "1", "LIonCore_M3_HasKey_key"), "LIonCore_M3")),
                LIonCore_M3.getProperties());
        assertEquals(Arrays.asList(new SerializedContainmentValue(new MetaPointer("LIonCore_M3", "1", "LIonCore_M3_Metamodel_elements"),
                Arrays.asList(
                        "LIonCore_M3_Concept",
                        "LIonCore_M3_ConceptInterface",
                        "LIonCore_M3_Containment",
                        "LIonCore_M3_DataType",
                        "LIonCore_M3_Enumeration",
                        "LIonCore_M3_EnumerationLiteral",
                        "LIonCore_M3_Feature",
                        "LIonCore_M3_FeaturesContainer",
                        "LIonCore_M3_HasKey",
                        "LIonCore_M3_Link",
                        "LIonCore_M3_Metamodel",
                        "LIonCore_M3_MetamodelElement",
                        "LIonCore_M3_NamespacedEntity",
                        "LIonCore_M3_NamespaceProvider",
                        "LIonCore_M3_PrimitiveType",
                        "LIonCore_M3_Property",
                        "LIonCore_M3_Reference"
                ))), LIonCore_M3.getContainments());
        assertEquals(Arrays.asList(
                        new SerializedReferenceValue(new MetaPointer("LIonCore_M3", "1", "LIonCore_M3_Metamodel_dependsOn"), Collections.emptyList())),
                LIonCore_M3.getReferences());

        SerializedNode LIonCore_M3_NamespacedEntity = serializedChunk.getNodes().stream().filter(n -> n.getID().equals("LIonCore_M3_NamespacedEntity")).findFirst().get();
        SerializedNode LIonCore_M3_NamespacedEntity_simpleName = serializedChunk.getNodes().stream().filter(n -> n.getID().equals("LIonCore_M3_NamespacedEntity_simpleName")).findFirst().get();
        SerializedNode LIonCore_M3_ConceptInterface_extends = serializedChunk.getNodes().stream().filter(n -> n.getID().equals("LIonCore_M3_ConceptInterface_extends")).findFirst().get();
    }

    @Test
    public void serializeLionCoreToJSON() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
        JsonElement serializedElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        JsonElement reserialized = jsonSerialization.serializeTreeToJsonElement(LionCore.getInstance());
        assertEquivalentLionWebJson(serializedElement.getAsJsonObject(), reserialized.getAsJsonObject());
    }

    @Test
    public void unserializeLionCoreToSerializedChunk() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        SerializedChunk serializedChunk = new LowLevelJsonSerialization().unserializeSerializationBlock(jsonElement);
        List<SerializedNode> unserializedSerializedNodeData = serializedChunk.getNodes();

        SerializedNode lioncore = serializedChunk.getNodeByID("LIonCore_M3");
        assertEquals(MetaPointer.from(LionCore.getMetamodel()), lioncore.getConcept());
        assertEquals("LIonCore_M3", lioncore.getID());
        assertEquals("LIonCore.M3", lioncore.getPropertyValue("LIonCore_M3_Metamodel_name"));
        assertEquals(17, lioncore.getChildren().size());
        assertEquals(null, lioncore.getParentNodeID());

        SerializedNode namespacedEntity = serializedChunk.getNodeByID("LIonCore_M3_NamespacedEntity");
        assertEquals(MetaPointer.from(LionCore.getConcept()), namespacedEntity.getConcept());
        assertEquals("LIonCore_M3_NamespacedEntity", namespacedEntity.getID());
        assertEquals("true", namespacedEntity.getPropertyValue("LIonCore_M3_Concept_abstract"));
        assertEquals("NamespacedEntity", namespacedEntity.getPropertyValue("LIonCore_M3_NamespacedEntity_simpleName"));
        assertEquals(2, namespacedEntity.getChildren().size());
        assertEquals(lioncore.getID(), namespacedEntity.getParentNodeID());

        SerializedNode simpleName = serializedChunk.getNodeByID("LIonCore_M3_NamespacedEntity_simpleName");
        assertEquals(MetaPointer.from(LionCore.getProperty()), simpleName.getConcept());
        assertEquals("simpleName", simpleName.getPropertyValue("LIonCore_M3_NamespacedEntity_simpleName"));
        assertEquals("LIonCore_M3_NamespacedEntity", simpleName.getParentNodeID());
        assertEquals(Arrays.asList(new SerializedReferenceValue.Entry("LIonCore_M3_String", "String")), simpleName.getReferenceValues("LIonCore_M3_Property_type"));
    }
    @Test
    public void unserializeLionCoreToConcreteClasses() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        List<Node> unserializedNodes = jsonSerialization.unserializeToNode(jsonElement);

        Metamodel lioncore = (Metamodel) unserializedNodes.get(0);
        assertEquals(LionCore.getMetamodel(), lioncore.getConcept());
        assertEquals("LIonCore_M3", lioncore.getID());
        assertEquals("LIonCore.M3", lioncore.getName());
        assertEquals(17, lioncore.getChildren().size());
        assertEquals(null, lioncore.getParent());

        Concept namespacedEntity = conceptByID(unserializedNodes, "LIonCore_M3_NamespacedEntity");
        assertEquals(LionCore.getConcept(), namespacedEntity.getConcept());
        assertEquals("LIonCore_M3_NamespacedEntity", namespacedEntity.getID());
        assertEquals(true, namespacedEntity.isAbstract());
        assertEquals("NamespacedEntity", namespacedEntity.getSimpleName());
        assertEquals(2, namespacedEntity.getChildren().size());
        assertEquals(lioncore, namespacedEntity.getParent());

        Property simpleName = propertyByID(unserializedNodes, "LIonCore_M3_NamespacedEntity_simpleName");
        assertEquals(LionCore.getProperty(), simpleName.getConcept());
        assertEquals("simpleName", simpleName.getSimpleName());
        assertEquals("LIonCore_M3_NamespacedEntity", simpleName.getParent().getID());
        assertEquals("LIonCore_M3_String", simpleName.getType().getID());
    }

    @Test
    public void unserializeLionCoreToDynamicNodes() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getBasicSerialization();
        jsonSerialization.getNodeResolver().addAll(LionCore.getInstance().thisAndAllDescendants());
        jsonSerialization.getNodeResolver().addAll(LionCoreBuiltins.getInstance().thisAndAllDescendants());
        jsonSerialization.getConceptResolver().registerMetamodel(LionCore.getInstance());
        jsonSerialization.getNodeInstantiator().enableDynamicNodes();
        jsonSerialization.getPrimitiveValuesSerialization().registerLionBuiltinsPrimitiveSerializersAndUnserializers();
        List<Node> unserializedNodes = jsonSerialization.unserializeToNode(jsonElement);

        DynamicNode lioncore = (DynamicNode) unserializedNodes.get(0);
        assertEquals(LionCore.getMetamodel(), lioncore.getConcept());
        assertEquals("LIonCore_M3", lioncore.getID());
        assertEquals("LIonCore.M3", lioncore.getPropertyValueByName("name"));
        assertEquals(17, lioncore.getChildren().size());
        assertEquals(null, lioncore.getParent());

        DynamicNode namespacedEntity = dynamicNodeByID(unserializedNodes, "LIonCore_M3_NamespacedEntity");
        assertEquals(LionCore.getConcept(), namespacedEntity.getConcept());
        assertEquals("LIonCore_M3_NamespacedEntity", namespacedEntity.getID());
        assertEquals(true, namespacedEntity.getPropertyValueByName("abstract"));
        assertEquals("NamespacedEntity", namespacedEntity.getPropertyValueByName("simpleName"));
        assertEquals(2, namespacedEntity.getChildren().size());
        assertEquals(lioncore, namespacedEntity.getParent());

        DynamicNode simpleName = dynamicNodeByID(unserializedNodes, "LIonCore_M3_NamespacedEntity_simpleName");
        assertEquals(LionCore.getProperty(), simpleName.getConcept());
        assertEquals("simpleName", simpleName.getPropertyValueByName("simpleName"));
        assertEquals("LIonCore_M3_NamespacedEntity", simpleName.getParent().getID());
    }

    @Test(expected = RuntimeException.class)
    public void unserializeLionCoreFailsWithoutRegisteringTheClassesOrEnablingDynamicNodes() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getBasicSerialization();
        jsonSerialization.getConceptResolver().registerMetamodel(LionCore.getInstance());
        jsonSerialization.getPrimitiveValuesSerialization().registerLionBuiltinsPrimitiveSerializersAndUnserializers();
        jsonSerialization.unserializeToNode(jsonElement);
    }

}
