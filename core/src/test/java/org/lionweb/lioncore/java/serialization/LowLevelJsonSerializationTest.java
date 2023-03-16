package org.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.serialization.data.MetaPointer;
import org.lionweb.lioncore.java.serialization.data.SerializationBlock;
import org.lionweb.lioncore.java.serialization.data.SerializedNode;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LowLevelJsonSerializationTest {

    @Test
    public void unserializeLionCoreToSerializedNodes() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
        SerializationBlock serializationBlock = jsonSerialization.readSerializationBlock(jsonElement);
        List<SerializedNode> unserializedSerializedNodeData = serializationBlock.getNodes();

        SerializedNode lioncore = unserializedSerializedNodeData.get(0);
        assertEquals(new MetaPointer("LIonCore_M3", "1", "LIonCore_M3_Metamodel"), lioncore.getConcept());
        assertEquals("LIonCore_M3", lioncore.getID());
        assertEquals("LIonCore.M3", lioncore.getPropertyValue("LIonCore_M3_Metamodel_name"));
        assertEquals(17, lioncore.getChildren().size());
        assertEquals(null, lioncore.getParentNodeID());

        SerializedNode namespacedEntity = unserializedSerializedNodeData.stream().filter(e -> e.getID().equals("LIonCore_M3_NamespacedEntity")).findFirst().get();
        assertEquals(new MetaPointer("LIonCore_M3", "1", "LIonCore_M3_Concept"), namespacedEntity.getConcept());
        assertEquals("LIonCore_M3_NamespacedEntity", namespacedEntity.getID());
        assertEquals("true", namespacedEntity.getPropertyValue("LIonCore_M3_Concept_abstract"));
        assertEquals("NamespacedEntity", namespacedEntity.getPropertyValue("LIonCore_M3_NamespacedEntity_simpleName"));
        assertEquals(2, namespacedEntity.getChildren().size());
        assertEquals(lioncore.getID(), namespacedEntity.getParentNodeID());

        SerializedNode simpleName = unserializedSerializedNodeData.stream().filter(e -> e.getID().equals("LIonCore_M3_NamespacedEntity_simpleName")).findFirst().get();
        assertEquals(new MetaPointer("LIonCore_M3", "1", "LIonCore_M3_Property"), simpleName.getConcept());
        assertEquals("simpleName", simpleName.getPropertyValue("LIonCore_M3_NamespacedEntity_simpleName"));
        assertEquals("LIonCore_M3_NamespacedEntity", simpleName.getParentNodeID());
        assertEquals(Arrays.asList(new SerializedNode.RawReferenceValue("LIonCore_M3_String", null)), simpleName.getReferenceValues("LIonCore_M3_Property_type"));
    }

    @Test
    public void unserializeLibraryMetamodel() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/library-metamodel.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
        SerializationBlock serializationBlock = jsonSerialization.readSerializationBlock(jsonElement);
        SerializedNode book = serializationBlock.getNodeByID("OcDK2GESljInG-ApIqtkXUoA2UeviB97u0UuiZzM0Hs");
        //assertEquals("Book", book.getPropertyValueByName("simpleName"));

        SerializedNode guidedBookWriter = serializationBlock.getNodeByID("nNUEzZ7it7d2HoHPAtk5rGO4SsqVA3hAlBwkK1KP8QU");
        //assertEquals("GuideBookWriter", guidedBookWriter.getPropertyValueByName("simpleName"));
        //assertNotNull(guidedBookWriter.getExtendedConcept());
    }

//    @Test
//    public void reserializeLibrary() {
//        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/library-metamodel.json");
//        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
//        LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
//        SerializationBlock serializationBlock = jsonSerialization.readSerializationBlock(jsonElement);
//        List<Node> unserializedNodes = jsonSerialization.unserializeToNode(jsonElement);
//        JsonElement reserialized = jsonSerialization.serialize(unserializedNodes.get(0));
//        assertEquivalentLionWebJson(jsonElement.getAsJsonObject(), reserialized.getAsJsonObject());
//    }



}
