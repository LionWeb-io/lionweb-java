package org.lionweb.lioncore.java.serialization;

import com.google.gson.*;
import org.junit.Ignore;
import org.junit.Test;
import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.ReferenceValue;
import org.lionweb.lioncore.java.model.impl.DynamicNode;
import org.lionweb.lioncore.java.self.LionCore;
import org.lionweb.lioncore.java.serialization.data.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;

public class JsonSerializationTest extends SerializationTest {

    @Test
    public void serializeReferenceWithoutResolveInfo() {
        Node book = new DynamicNode("foo123", LibraryMetamodel.BOOK);
        Node writer = new DynamicNode("_Arthur_Foozillus_id_", LibraryMetamodel.WRITER);
        book.addReferenceValue(LibraryMetamodel.BOOK.getReferenceByName("author"), new ReferenceValue(writer, null));

        // The library MM is not using the standard primitive types but its own, so we need to specify how to serialize
        // those values
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        jsonSerialization.getPrimitiveValuesSerialization().registerSerializer("INhBvWyXvxwNsePuX0rdNGB_J9hi85cTb1Q0APXCyJ0", (PrimitiveValuesSerialization.PrimitiveSerializer<String>) value -> value);
        jsonSerialization.getPrimitiveValuesSerialization().registerSerializer("gVp8_QSmXE2k4pd-sQZgjYMoW95SLLaVIH4yMYqqbt4", (PrimitiveValuesSerialization.PrimitiveSerializer<Integer>) value -> value.toString());
        JsonObject jsonSerialized = jsonSerialization.serializeNodesToJson(book).getAsJsonObject();
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/foo-library.json");
        JsonObject jsonRead = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        assertEquivalentLionWebJson(jsonRead, jsonSerialized);
    }

    @Test
    public void serializeMultipleSubtrees() {
        Library bobsLibrary = new Library("bl", "Bob's Library");
        GuideBookWriter jackLondon = new GuideBookWriter("jl", "Jack London");
        jackLondon.setCountries("Alaska");
        Book explorerBook = new Book("eb", "Explorer Book", jackLondon);
        bobsLibrary.addBook(explorerBook);
        assertEquals(Arrays.asList(explorerBook), bobsLibrary.getChildren());

        // The library MM is not using the standard primitive types but its own, so we need to specify how to serialize
        // those values
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        jsonSerialization.getPrimitiveValuesSerialization().registerSerializer("INhBvWyXvxwNsePuX0rdNGB_J9hi85cTb1Q0APXCyJ0", (PrimitiveValuesSerialization.PrimitiveSerializer<String>) value -> value);
        jsonSerialization.getPrimitiveValuesSerialization().registerSerializer("gVp8_QSmXE2k4pd-sQZgjYMoW95SLLaVIH4yMYqqbt4", (PrimitiveValuesSerialization.PrimitiveSerializer<Integer>) value -> value.toString());
        JsonObject jsonSerialized = jsonSerialization.serializeTreesToJson(bobsLibrary, jackLondon).getAsJsonObject();
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/bobslibrary.json");
        JsonObject jsonRead = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        assertEquivalentLionWebJson(jsonRead, jsonSerialized);
    }

    @Test
    public void serializeMultipleSubtreesSkipDuplicateNodes() {
        Library bobsLibrary = new Library("bl", "Bob's Library");
        GuideBookWriter jackLondon = new GuideBookWriter("jl", "Jack London");
        jackLondon.setCountries("Alaska");
        Book explorerBook = new Book("eb", "Explorer Book", jackLondon);
        bobsLibrary.addBook(explorerBook);

        // The library MM is not using the standard primitive types but its own, so we need to specify how to serialize
        // those values
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        jsonSerialization.getPrimitiveValuesSerialization().registerSerializer("INhBvWyXvxwNsePuX0rdNGB_J9hi85cTb1Q0APXCyJ0", (PrimitiveValuesSerialization.PrimitiveSerializer<String>) value -> value);
        jsonSerialization.getPrimitiveValuesSerialization().registerSerializer("gVp8_QSmXE2k4pd-sQZgjYMoW95SLLaVIH4yMYqqbt4", (PrimitiveValuesSerialization.PrimitiveSerializer<Integer>) value -> value.toString());
        JsonObject jsonSerialized = jsonSerialization.serializeNodesToJson(bobsLibrary, jackLondon, explorerBook).getAsJsonObject();
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/bobslibrary.json");
        JsonObject jsonRead = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        assertEquivalentLionWebJson(jsonRead, jsonSerialized);
    }

    @Test
    public void unserializeMetamodelWithEnumerations() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/TestLang-metamodel.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        List<Node> unserializedNodes = jsonSerialization.unserializeToNode(jsonElement);

        Enumeration testEnumeration1 = (Enumeration) unserializedNodes.stream().filter(n -> n.getID().equals("MDhjYWFkNzUtODI0Ni00NDI3LWJiNGQtODQ0NGI2YzVjNzI5LzI1ODUzNzgxNjU5NzMyMDQ1ODI")).findFirst().get();
        assertEquals("TestEnumeration1", testEnumeration1.getSimpleName());
        assertEquals(2, testEnumeration1.getLiterals().size());

        Concept sideTransformInfo = (Concept) unserializedNodes.stream().filter(n -> n.getID().equals("Y2VhYjUxOTUtMjVlYS00ZjIyLTliOTItMTAzYjk1Y2E4YzBjLzc3OTEyODQ5Mjg1MzM2OTE2NQ")).findFirst().get();
        assertEquals("SideTransformInfo", sideTransformInfo.getSimpleName());
        assertEquals(false, sideTransformInfo.isAbstract());
        assertEquals(3, sideTransformInfo.getFeatures().size());
        assertEquals(3, sideTransformInfo.getChildren().size());
    }

    @Test
    public void reserializeMetamodelWithEnumerations() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/TestLang-metamodel.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        List<Node> unserializedNodes = jsonSerialization.unserializeToNode(jsonElement);
        JsonObject reserialized = jsonSerialization.serializeNodesToJson(unserializedNodes).getAsJsonObject();

        List<JsonObject> metamodels = getNodesByConcept(reserialized.get("nodes").getAsJsonArray(), "LIonCore_M3_Metamodel");
        assertEquals(2, metamodels.size());

        List<JsonObject> concepts = getNodesByConcept(reserialized.get("nodes").getAsJsonArray(), "LIonCore_M3_Concept");
        assertEquals(19, concepts.size());

        assertEquivalentLionWebJson(jsonElement.getAsJsonObject(), reserialized);
    }

    @Test
    public void converter() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/TestLang-metamodel.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));

        jsonElement.getAsJsonObject().get("nodes").getAsJsonArray().forEach(e -> convertNode(e.getAsJsonObject()));

        System.out.println(new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(jsonElement));
    }

    private void convertNode(JsonObject node) {
        System.out.println("converting node " + node);
        String conceptId = node.get("concept").getAsString();
        JsonObject concept = new JsonObject();
        concept.addProperty("metamodel", "LIonCore_M3");
        concept.addProperty("version", "1");
        concept.addProperty("key", conceptId);
        node.remove("concept");
        node.add("concept", concept);

        JsonObject oldProperties = node.get("properties").getAsJsonObject();
        JsonArray newProperties = new JsonArray();
        oldProperties.keySet().forEach(propertyID -> {
            JsonObject newProperty = new JsonObject();
            JsonObject newPropertyMP = new JsonObject();
            newPropertyMP.addProperty("metamodel", "LIonCore_M3");
            newPropertyMP.addProperty("version", "1");
            newPropertyMP.addProperty("key", propertyID);
            newProperty.add("property", newPropertyMP);
            newProperty.add("value", oldProperties.get(propertyID));
            newProperties.add(newProperty);
        });
        node.remove("properties");
        node.add("properties", newProperties);

        JsonObject oldChildren = node.get("children").getAsJsonObject();
        JsonArray newChildren = new JsonArray();
        oldChildren.keySet().forEach(containmentID -> {
            JsonObject newProperty = new JsonObject();
            JsonObject newPropertyMP = new JsonObject();
            newPropertyMP.addProperty("metamodel", "LIonCore_M3");
            newPropertyMP.addProperty("version", "1");
            newPropertyMP.addProperty("key", containmentID);
            newProperty.add("containment", newPropertyMP);
            newProperty.add("children", oldChildren.get(containmentID));
            newChildren.add(newProperty);
        });
        node.remove("children");
        node.add("children", newChildren);

        JsonObject oldReferences = node.get("references").getAsJsonObject();
        JsonArray newReferences = new JsonArray();
        oldReferences.keySet().forEach(referenceID -> {
            JsonObject newProperty = new JsonObject();
            JsonObject newPropertyMP = new JsonObject();
            newPropertyMP.addProperty("metamodel", "LIonCore_M3");
            newPropertyMP.addProperty("version", "1");
            newPropertyMP.addProperty("key", referenceID);
            newProperty.add("reference", newPropertyMP);

            JsonArray oldTargets = oldReferences.get(referenceID).getAsJsonArray();
//            JsonArray targets = new JsonArray();
//            oldTargets.forEach(e -> {
//                JsonObject jo = new JsonObject();
//                jo.add("resolveInfo", new JsonNull());
//                jo.add("reference", e);
//                targets.add(jo);
//            });

            newProperty.add("targets", oldTargets);
            newReferences.add(newProperty);
        });
        node.remove("references");
        node.add("references", newReferences);
    }
}
