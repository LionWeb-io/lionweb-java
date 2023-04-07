package org.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.lionweb.lioncore.java.serialization.data.MetaPointer;
import org.lionweb.lioncore.java.serialization.data.SerializedChunk;
import org.lionweb.lioncore.java.serialization.data.SerializedNode;
import org.lionweb.lioncore.java.serialization.data.SerializedReferenceValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;

public class LowLevelJsonSerializationTest {

    @Test
    public void unserializeLionCoreToSerializedNodes() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
        SerializedChunk serializedChunk = jsonSerialization.unserializeSerializationBlock(jsonElement);
        List<SerializedNode> unserializedSerializedNodeData = serializedChunk.getNodes();

        SerializedNode lioncore = unserializedSerializedNodeData.get(0);
        assertEquals(new MetaPointer("LIonCore_M3", "1", "Metamodel"), lioncore.getConcept());
        assertEquals("LIonCore_M3", lioncore.getID());
        assertEquals("LIonCore.M3", lioncore.getPropertyValue("Metamodel_name"));
        assertEquals(17, lioncore.getChildren().size());
        assertEquals(null, lioncore.getParentNodeID());

        SerializedNode namespacedEntity = unserializedSerializedNodeData.stream().filter(e -> e.getID().equals("LIonCore_M3_NamespacedEntity")).findFirst().get();
        assertEquals(new MetaPointer("LIonCore_M3", "1", "Concept"), namespacedEntity.getConcept());
        assertEquals("LIonCore_M3_NamespacedEntity", namespacedEntity.getID());
        assertEquals("true", namespacedEntity.getPropertyValue("Concept_abstract"));
        assertEquals("NamespacedEntity", namespacedEntity.getPropertyValue("NamespacedEntity_simpleName"));
        assertEquals(2, namespacedEntity.getChildren().size());
        assertEquals(lioncore.getID(), namespacedEntity.getParentNodeID());

        SerializedNode simpleName = unserializedSerializedNodeData.stream().filter(e -> e.getID().equals("LIonCore_M3_NamespacedEntity_simpleName")).findFirst().get();
        assertEquals(new MetaPointer("LIonCore_M3", "1", "Property"), simpleName.getConcept());
        assertEquals("simpleName", simpleName.getPropertyValue("NamespacedEntity_simpleName"));
        assertEquals("LIonCore_M3_NamespacedEntity", simpleName.getParentNodeID());
        assertEquals(Arrays.asList(new SerializedReferenceValue.Entry("LIonCore_M3_String", "String")), simpleName.getReferenceValues("Property_type"));
    }

    @Test
    public void unserializeLibraryMetamodelToSerializedNodes() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/library-metamodel.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
        SerializedChunk serializedChunk = jsonSerialization.unserializeSerializationBlock(jsonElement);
        SerializedNode book = serializedChunk.getNodeByID("library-Book");
        assertEquals("Book", book.getPropertyValue("NamespacedEntity_simpleName"));

        SerializedNode guidedBookWriter = serializedChunk.getNodeByID("library-GuideBookWriter");
        assertEquals("GuideBookWriter", guidedBookWriter.getPropertyValue("NamespacedEntity_simpleName"));
        assertEquals(Arrays.asList(new SerializedReferenceValue.Entry("library-Writer", "Writer")), guidedBookWriter.getReferenceValues("Concept_extends"));
    }

    @Test
    public void reserializeLibraryMetamodel() {
        assertTheFileIsReserializedFromLowLevelCorrectly("/serialization/library-metamodel.json");
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
        JsonElement reserialized = jsonSerialization.serializeToJson(serializedChunk);
        assertEquivalentLionWebJson(jsonElement.getAsJsonObject(), reserialized.getAsJsonObject());
    }

}
