package org.lionweb.lioncore.java.serialization;

import com.google.gson.*;
import org.junit.Ignore;
import org.junit.Test;
import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.Metamodel;
import org.lionweb.lioncore.java.metamodel.Property;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.self.LionCore;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class JsonSerializationTest {

    @Test
    public void unserializeLionCore() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        List<Node> unserializedNodes = jsonSerialization.unserialize(jsonElement);

        Metamodel lioncore = (Metamodel) unserializedNodes.get(0);
        assertEquals(LionCore.getMetamodel(), lioncore.getConcept());
        assertEquals("LIonCore_M3", lioncore.getID());
        assertEquals("LIonCore.M3", lioncore.getQualifiedName());
        assertEquals(16, lioncore.getChildren().size());
        assertEquals(null, lioncore.getParent());

        Concept namespacedEntity = (Concept) unserializedNodes.get(1);
        assertEquals(LionCore.getConcept(), namespacedEntity.getConcept());
        assertEquals("LIonCore_M3_NamespacedEntity", namespacedEntity.getID());
        assertEquals(true, namespacedEntity.isAbstract());
        assertEquals("NamespacedEntity", namespacedEntity.getSimpleName());
        assertEquals(2, namespacedEntity.getChildren().size());
        assertEquals(lioncore, namespacedEntity.getParent());

        Property simpleName = (Property) unserializedNodes.get(2);
        assertEquals(LionCore.getProperty(), simpleName.getConcept());
        assertEquals("simpleName", simpleName.getSimpleName());
        assertEquals("LIonCore_M3_NamespacedEntity", simpleName.getParent().getID());
        assertEquals("LIonCore_M3_String", simpleName.getType().getID());
    }

    @Ignore // Eventually we should have the same serialization. Right now there are differences in the LionCore M3 that we need to solve
    @Test
    public void serializeLionCore() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
        JsonElement serializedElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        JsonElement reserialized = jsonSerialization.serialize(LionCore.getMetamodel());
        assertEquals(serializedElement, reserialized);
    }

    @Test
    public void unserializeLibrary() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/library-metamodel.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        List<Node> unserializedNodes = jsonSerialization.unserialize(jsonElement);
    }

    @Test
    public void reserializeLibrary() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/library-metamodel.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        List<Node> unserializedNodes = jsonSerialization.unserialize(jsonElement);
        JsonElement reserialized = jsonSerialization.serialize(unserializedNodes.get(0));
        assertEquivalentLionWebJson(jsonElement.getAsJsonArray(), reserialized.getAsJsonArray());
    }

    private void assertEquivalentLionWebJson(JsonArray expected, JsonArray actual) {
        Map<String, JsonObject> expectedElements = new HashMap<>();
        Map<String, JsonObject> actualElements = new HashMap<>();
        expected.forEach(e -> {
            String id = e.getAsJsonObject().get("id").getAsString();
            expectedElements.put(id, e.getAsJsonObject());
        });
        actual.forEach(e -> {
            String id = e.getAsJsonObject().get("id").getAsString();
            actualElements.put(id, e.getAsJsonObject());
        });
        Set<String> unexpectedIDs = new HashSet<>(actualElements.keySet());
        unexpectedIDs.removeAll(expectedElements.keySet());
        Set<String> missingIDs = new HashSet<>(expectedElements.keySet());
        missingIDs.removeAll(actualElements.keySet());
        if (!unexpectedIDs.isEmpty()) {
            throw new AssertionError("Unexpected IDs found: " + unexpectedIDs);
        }
        if (!missingIDs.isEmpty()) {
            throw new AssertionError("Missing IDs found: " + missingIDs);
        }
        for (String id: expectedElements.keySet()) {
            JsonObject expectedElement = expectedElements.get(id);
            JsonObject actualElement = actualElements.get(id);
            assertEquivalentNodes(expectedElement, actualElement, "Node " + id);
        }
    }

    private void assertEquivalentNodes(JsonObject expected, JsonObject actual, String context) {
        Set<String> actualMeaningfulKeys = actual.keySet();
        Set<String> expectedMeaningfulKeys = expected.keySet();

        Set<String> unexpectedKeys = new HashSet<>(actualMeaningfulKeys);
        unexpectedKeys.removeAll(expectedMeaningfulKeys);
        Set<String> missingKeys = new HashSet<>(expectedMeaningfulKeys);
        missingKeys.removeAll(actualMeaningfulKeys);
        if (!unexpectedKeys.isEmpty()) {
            throw new AssertionError("(" + context + ") Unexpected keys found: " + unexpectedKeys);
        }
        if (!missingKeys.isEmpty()) {
            throw new AssertionError("(" + context + ") Missing keys found: " + missingKeys);
        }
        for (String key: actualMeaningfulKeys) {
            if (key.equals("parent")) {
                assertEquals("(" + context + ") different parent", expected.get("parent"), actual.get("parent"));
            } else if (key.equals("concept")) {
                assertEquals("(" + context + ") different concept", expected.get("concept"), actual.get("concept"));
            } else if (key.equals("id")) {
                assertEquals("(" + context + ") different id", expected.get("id"), actual.get("id"));
            } else if (key.equals("references")) {
                assertEquivalentObjects(expected.getAsJsonObject("references"), actual.getAsJsonObject("references"), "References of " + context);
            } else if (key.equals("children")) {
                assertEquivalentObjects(expected.getAsJsonObject("children"), actual.getAsJsonObject("children"), "Children of " + context);
            } else if (key.equals("properties")) {
                assertEquivalentObjects(expected.getAsJsonObject("properties"), actual.getAsJsonObject("properties"), "Properties of " + context);
            } else {
                throw new UnsupportedOperationException(key);
            }
        }
    }

    private void assertEquivalentObjects(JsonObject expected, JsonObject actual, String context) {
        Set<String> actualMeaningfulKeys = actual.keySet().stream().filter(k ->
                !actual.get(k).equals(new JsonObject())
                        && !actual.get(k).equals(new JsonArray())
                        && !actual.get(k).equals(JsonNull.INSTANCE)).collect(Collectors.toSet());
        Set<String> expectedMeaningfulKeys = expected.keySet().stream().filter(k -> !expected.get(k).equals(new JsonObject())
                && !expected.get(k).equals(new JsonArray())
                && !expected.get(k).equals(JsonNull.INSTANCE)).collect(Collectors.toSet());

        Set<String> unexpectedKeys = new HashSet<>(actualMeaningfulKeys);
        unexpectedKeys.removeAll(expectedMeaningfulKeys);
        Set<String> missingKeys = new HashSet<>(expectedMeaningfulKeys);
        missingKeys.removeAll(actualMeaningfulKeys);
        if (!unexpectedKeys.isEmpty()) {
            throw new AssertionError("(" + context + ") Unexpected keys found: " + unexpectedKeys);
        }
        if (!missingKeys.isEmpty()) {
            throw new AssertionError("(" + context + ") Missing keys found: " + missingKeys);
        }
        for (String key: actualMeaningfulKeys) {
            assertEquals("(" + context + ") Different values for key " + key, expected.get(key), actual.get(key));
        }
    }

}
