package org.lionweb.lioncore.java.serialization;

import com.google.gson.*;
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

public class JsonSerializationTest {

    @Test
    public void serializeLionCoreToSerializationBlock() {
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        SerializationBlock serializationBlock = jsonSerialization.serializeTreeToSerializationBlock(LionCore.getInstance());

        assertEquals("1", serializationBlock.getSerializationFormatVersion());

        assertEquals(1, serializationBlock.getMetamodels().size());
        assertEquals(new MetamodelKeyVersion("LIonCore_M3", "1"), serializationBlock.getMetamodels().get(0));

        SerializedNode LIonCore_M3 = serializationBlock.getNodes().stream().filter(n -> n.getID().equals("LIonCore_M3")).findFirst().get();
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

        SerializedNode LIonCore_M3_NamespacedEntity = serializationBlock.getNodes().stream().filter(n -> n.getID().equals("LIonCore_M3_NamespacedEntity")).findFirst().get();
        SerializedNode LIonCore_M3_NamespacedEntity_simpleName = serializationBlock.getNodes().stream().filter(n -> n.getID().equals("LIonCore_M3_NamespacedEntity_simpleName")).findFirst().get();
        SerializedNode LIonCore_M3_ConceptInterface_extends = serializationBlock.getNodes().stream().filter(n -> n.getID().equals("LIonCore_M3_ConceptInterface_extends")).findFirst().get();
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

    private DynamicNode dynamicNodeByID(List<Node> nodes, String id) {
        return (DynamicNode) nodes.stream().filter(e -> e.getID().equals(id)).findFirst().get();
    }

    private Concept conceptByID(List<Node> nodes, String id) {
        return (Concept) nodes.stream().filter(e -> e.getID().equals(id)).findFirst().get();
    }

    private Property propertyByID(List<Node> nodes, String id) {
        return (Property) nodes.stream().filter(e -> e.getID().equals(id)).findFirst().get();
    }

//    @Test
//    public void unserializeLionCoreToNodeData() {
//        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
//        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
//        JsonSerialization jsonSerialization = JsonSerialization.getBasicSerialization();
//        List<SerializedNode> unserializedSerializedNodeData = jsonSerialization.rawUnserialization(jsonElement);
//
//        SerializedNode lioncore = unserializedSerializedNodeData.get(0);
//        assertEquals(LionCore.getMetamodel().getID(), lioncore.getConcept());
//        assertEquals("LIonCore_M3", lioncore.getID());
//        assertEquals("LIonCore.M3", lioncore.getPropertyValue("LIonCore_M3_Metamodel_name"));
//        assertEquals(16, lioncore.getChildren().size());
//        assertEquals(null, lioncore.getParentNodeID());
//
//        SerializedNode namespacedEntity = unserializedSerializedNodeData.get(1);
//        assertEquals(LionCore.getConcept().getID(), namespacedEntity.getConcept());
//        assertEquals("LIonCore_M3_NamespacedEntity", namespacedEntity.getID());
//        assertEquals("true", namespacedEntity.getPropertyValue("LIonCore_M3_Concept_abstract"));
//        assertEquals("NamespacedEntity", namespacedEntity.getPropertyValue("LIonCore_M3_NamespacedEntity_simpleName"));
//        assertEquals(2, namespacedEntity.getChildren().size());
//        assertEquals(lioncore.getID(), namespacedEntity.getParentNodeID());
//
//        SerializedNode simpleName = unserializedSerializedNodeData.get(2);
//        assertEquals(LionCore.getProperty().getID(), simpleName.getConcept());
//        assertEquals("simpleName", simpleName.getPropertyValue("LIonCore_M3_NamespacedEntity_simpleName"));
//        assertEquals("LIonCore_M3_NamespacedEntity", simpleName.getParentNodeID());
//        assertEquals(Arrays.asList(new SerializedNode.RawReferenceValue("LIonCore_M3_String", null)), simpleName.getReferenceValues("LIonCore_M3_Property_type"));
//    }

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

    //@Ignore // Eventually we should have the same serialization. Right now there are differences in the LionCore M3 that we need to solve
    @Test
    public void serializeLionCore() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
        JsonElement serializedElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        JsonElement reserialized = jsonSerialization.serializeTreeToJson(LionCore.getInstance());
        System.out.println(new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(reserialized));
        assertEquivalentLionWebJson(serializedElement.getAsJsonObject(), reserialized.getAsJsonObject());
    }

    @Test
    public void unserializeLibrary() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/library-metamodel.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        List<Node> unserializedNodes = jsonSerialization.unserializeToNode(jsonElement);
        Node book = unserializedNodes.stream().filter(n -> n.getID().equals("library-Book")).findFirst().get();
        assertEquals("Book", book.getPropertyValueByName("simpleName"));

        Concept guidedBookWriter = (Concept) unserializedNodes.stream().filter(n -> n.getID().equals("library-GuideBookWriter")).findFirst().get();
        assertEquals("GuideBookWriter", guidedBookWriter.getPropertyValueByName("simpleName"));
        assertNotNull(guidedBookWriter.getExtendedConcept());
    }

    @Test
    public void reserializeLibrary() {
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/library-metamodel.json");
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        List<Node> unserializedNodes = jsonSerialization.unserializeToNode(jsonElement);
        JsonElement reserialized = jsonSerialization.serializeTreeToJson(unserializedNodes.get(0));
        System.out.println(new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(reserialized));
        assertEquivalentLionWebJson(jsonElement.getAsJsonObject(), reserialized.getAsJsonObject());
    }

    @Test
    public void serializeLibraryInstance() {
        Library library = new Library("lib-1", "Language Engineering Library");
        Writer mv = new Writer("mv", "Markus Völter");
        Writer mb = new Writer("mb", "Meinte Boersma");
        Book de = new Book("de", "DSL Engineering", mv).setPages(558);
        Book bfd = new Book("bfd", "Business-Friendly DSLs", mb).setPages(517);
        library.addBook(de);
        library.addBook(bfd);

        // The library MM is not using the standard primitive types but its own, so we need to specify how to serialize
        // those values
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        jsonSerialization.getPrimitiveValuesSerialization().registerSerializer("INhBvWyXvxwNsePuX0rdNGB_J9hi85cTb1Q0APXCyJ0", (PrimitiveValuesSerialization.PrimitiveSerializer<String>) value -> value);
        jsonSerialization.getPrimitiveValuesSerialization().registerSerializer("gVp8_QSmXE2k4pd-sQZgjYMoW95SLLaVIH4yMYqqbt4", (PrimitiveValuesSerialization.PrimitiveSerializer<Integer>) value -> value.toString());
        JsonObject jsonSerialized = jsonSerialization.serializeNodesToJson(library).getAsJsonObject();
        InputStream inputStream = this.getClass().getResourceAsStream("/serialization/langeng-library.json");
        JsonObject jsonRead = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        assertEquivalentLionWebJson(jsonRead, jsonSerialized);
    }

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
        JsonObject jsonSerialized = jsonSerialization.serializeNodesToJson(bobsLibrary, jackLondon).getAsJsonObject();
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
    public void serializeBoolean() {
        MyNodeWithProperties node = new MyNodeWithProperties("n1");
        node.setP1(true);

        JsonObject expected = JsonParser.parseString("{\n" +
                "  \"serializationFormatVersion\": \"1\",\n" +
                "  \"nodes\": [{\n" +
                "    \"concept\": \"concept-MyNodeWithProperties\",\n" +
                "    \"id\": \"n1\",\n" +
                "    \"properties\": {\n" +
                "      \"p1\": \"true\"\n" +
                "    },\n" +
                "    \"children\": {},\n" +
                "    \"references\": {}\n" +
                "  }]}").getAsJsonObject();
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        JsonObject serialized = jsonSerialization.serializeNodesToJson(node).getAsJsonObject();
        assertEquivalentLionWebJson(expected, serialized);
    }

    @Test
    public void unserializeBoolean() {
        MyNodeWithProperties node = new MyNodeWithProperties("n1");
        node.setP1(true);

        JsonObject serialized = JsonParser.parseString("{\n" +
                "  \"serializationFormatVersion\": \"1\",\n" +
                "  \"nodes\": [{\n" +
                "    \"concept\": \"concept-MyNodeWithProperties\",\n" +
                "    \"id\": \"n1\",\n" +
                "    \"properties\": {\n" +
                "      \"p1\": \"true\"\n" +
                "    },\n" +
                "    \"children\": {},\n" +
                "    \"references\": {}\n" +
                "  }]}").getAsJsonObject();
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        jsonSerialization.getConceptResolver().registerMetamodel(MyNodeWithProperties.METAMODEL);
        jsonSerialization.getNodeInstantiator().registerCustomUnserializer(MyNodeWithProperties.CONCEPT.getID(), (concept, serializedNode) -> new MyNodeWithProperties(serializedNode.getID()));
        List<Node> unserialized = jsonSerialization.unserializeToNode(serialized);
        assertEquals(Arrays.asList(node), unserialized);
    }

    @Test
    public void serializeString() {
        MyNodeWithProperties node = new MyNodeWithProperties("n1");
        node.setP3("qwerty");

        JsonObject expected = JsonParser.parseString("{\n" +
                "  \"serializationFormatVersion\": \"1\",\n" +
                "  \"nodes\": [{\n" +
                "    \"concept\": \"concept-MyNodeWithProperties\",\n" +
                "    \"id\": \"n1\",\n" +
                "    \"properties\": {\n" +
                "      \"p3\": \"qwerty\"\n" +
                "    },\n" +
                "    \"children\": {},\n" +
                "    \"references\": {}\n" +
                "  }]}").getAsJsonObject();
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        JsonObject serialized = jsonSerialization.serializeNodesToJson(node).getAsJsonObject();
        assertEquivalentLionWebJson(expected, serialized);
    }

    @Test
    public void unserializeString() {
        MyNodeWithProperties node = new MyNodeWithProperties("n1");
        node.setP3("qwerty");

        JsonObject serialized = JsonParser.parseString("{\n" +
                "  \"serializationFormatVersion\": \"1\",\n" +
                "  \"nodes\": [{\n" +
                "    \"concept\": \"concept-MyNodeWithProperties\",\n" +
                "    \"id\": \"n1\",\n" +
                "    \"properties\": {\n" +
                "      \"p3\": \"qwerty\"\n" +
                "    },\n" +
                "    \"children\": {},\n" +
                "    \"references\": {}\n" +
                "  }]}").getAsJsonObject();
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        jsonSerialization.getConceptResolver().registerMetamodel(MyNodeWithProperties.METAMODEL);
        jsonSerialization.getNodeInstantiator().registerCustomUnserializer(MyNodeWithProperties.CONCEPT.getID(), (concept, serializedNode) -> new MyNodeWithProperties(serializedNode.getID()));
        List<Node> unserialized = jsonSerialization.unserializeToNode(serialized);
        assertEquals(Arrays.asList(node), unserialized);
    }

    @Test
    public void serializeInteger() {
        MyNodeWithProperties node = new MyNodeWithProperties("n1");
        node.setP2(2904);

        JsonObject expected = JsonParser.parseString("{\n" +
                "  \"serializationFormatVersion\": \"1\",\n" +
                "  \"metamodels\": [],\n" +
                "  \"nodes\": [\n" +
                "    {\n" +
                "      \"id\": \"n1\",\n" +
                "      \"concept\": {\n" +
                "        \"metamodel\": \"mymetamodel\",\n" +
                "        \"version\": \"1\",\n" +
                "        \"key\": \"concept-MyNodeWithProperties\"\n" +
                "      },\n" +
                "      \"properties\": [\n" +
                "        {\n" +
                "          \"property\": {\n" +
                "            \"metamodel\": \"mymetamodel\",\n" +
                "            \"version\": \"1\",\n" +
                "            \"key\": \"p1\"\n" +
                "          },\n" +
                "          \"value\": null\n" +
                "        },\n" +
                "        {\n" +
                "          \"property\": {\n" +
                "            \"metamodel\": \"mymetamodel\",\n" +
                "            \"version\": \"1\",\n" +
                "            \"key\": \"p2\"\n" +
                "          },\n" +
                "          \"value\": \"2904\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"property\": {\n" +
                "            \"metamodel\": \"mymetamodel\",\n" +
                "            \"version\": \"1\",\n" +
                "            \"key\": \"p3\"\n" +
                "          },\n" +
                "          \"value\": null\n" +
                "        },\n" +
                "        {\n" +
                "          \"property\": {\n" +
                "            \"metamodel\": \"mymetamodel\",\n" +
                "            \"version\": \"1\",\n" +
                "            \"key\": \"p4\"\n" +
                "          },\n" +
                "          \"value\": null\n" +
                "        }\n" +
                "      ],\n" +
                "      \"children\": [],\n" +
                "      \"references\": [],\n" +
                "      \"parent\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}").getAsJsonObject();
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        JsonObject serialized = jsonSerialization.serializeNodesToJson(node).getAsJsonObject();
        System.out.println(new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(serialized));
        assertEquivalentLionWebJson(expected, serialized);
    }

    @Test
    public void unserializeInteger() {
        MyNodeWithProperties node = new MyNodeWithProperties("n1");
        node.setP2(2904);

        JsonObject serialized = JsonParser.parseString("{\n" +
                "  \"serializationFormatVersion\": \"1\",\n" +
                "  \"metamodels\": [],\n" +
                "  \"nodes\": [\n" +
                "    {\n" +
                "      \"id\": \"n1\",\n" +
                "      \"concept\": {\n" +
                "        \"metamodel\": \"mymetamodel\",\n" +
                "        \"version\": \"1\",\n" +
                "        \"key\": \"concept-MyNodeWithProperties\"\n" +
                "      },\n" +
                "      \"properties\": [\n" +
                "        {\n" +
                "          \"property\": {\n" +
                "            \"metamodel\": \"mymetamodel\",\n" +
                "            \"version\": \"1\",\n" +
                "            \"key\": \"p1\"\n" +
                "          },\n" +
                "          \"value\": null\n" +
                "        },\n" +
                "        {\n" +
                "          \"property\": {\n" +
                "            \"metamodel\": \"mymetamodel\",\n" +
                "            \"version\": \"1\",\n" +
                "            \"key\": \"p2\"\n" +
                "          },\n" +
                "          \"value\": \"2904\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"property\": {\n" +
                "            \"metamodel\": \"mymetamodel\",\n" +
                "            \"version\": \"1\",\n" +
                "            \"key\": \"p3\"\n" +
                "          },\n" +
                "          \"value\": null\n" +
                "        },\n" +
                "        {\n" +
                "          \"property\": {\n" +
                "            \"metamodel\": \"mymetamodel\",\n" +
                "            \"version\": \"1\",\n" +
                "            \"key\": \"p4\"\n" +
                "          },\n" +
                "          \"value\": null\n" +
                "        }\n" +
                "      ],\n" +
                "      \"children\": [],\n" +
                "      \"references\": [],\n" +
                "      \"parent\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}").getAsJsonObject();
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        jsonSerialization.getConceptResolver().registerMetamodel(MyNodeWithProperties.METAMODEL);
        jsonSerialization.getNodeInstantiator().registerCustomUnserializer(MyNodeWithProperties.CONCEPT.getID(), (concept, serializedNode) -> new MyNodeWithProperties(serializedNode.getID()));
        List<Node> unserialized = jsonSerialization.unserializeToNode(serialized);
        assertEquals(Arrays.asList(node), unserialized);
    }

    @Test
    public void serializeJSON() {
        MyNodeWithProperties node = new MyNodeWithProperties("n1");
        JsonArray ja = new JsonArray();
        ja.add(1);
        ja.add("foo");
        node.setP4(ja);

        JsonObject expected = JsonParser.parseString("{\n" +
                "  \"serializationFormatVersion\": \"1\",\n" +
                "  \"nodes\": [{\n" +
                "    \"concept\": \"concept-MyNodeWithProperties\",\n" +
                "    \"id\": \"n1\",\n" +
                "    \"properties\": {\n" +
                "      \"p4\": \"[1,\\\"foo\\\"]\"\n" +
                "    },\n" +
                "    \"children\": {},\n" +
                "    \"references\": {}\n" +
                "  }]}").getAsJsonObject();
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        JsonObject serialized = jsonSerialization.serializeNodesToJson(node).getAsJsonObject();
        assertEquivalentLionWebJson(expected, serialized);
    }

    @Test
    public void unserializeJSON() {
        MyNodeWithProperties node = new MyNodeWithProperties("n1");
        JsonArray ja = new JsonArray();
        ja.add(1);
        ja.add("foo");
        node.setP4(ja);

        JsonObject serialized = JsonParser.parseString("{\n" +
                "  \"serializationFormatVersion\": \"1\",\n" +
                "  \"nodes\": [{\n" +
                "    \"concept\": \"concept-MyNodeWithProperties\",\n" +
                "    \"id\": \"n1\",\n" +
                "    \"properties\": {\n" +
                "      \"p4\": \"[1,\\\"foo\\\"]\"\n" +
                "    },\n" +
                "    \"children\": {},\n" +
                "    \"references\": {}\n" +
                "  }]}").getAsJsonObject();
        JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
        jsonSerialization.getConceptResolver().registerMetamodel(MyNodeWithProperties.METAMODEL);
        jsonSerialization.getNodeInstantiator().registerCustomUnserializer(MyNodeWithProperties.CONCEPT.getID(), (concept, serializedNode) -> new MyNodeWithProperties(serializedNode.getID()));
        List<Node> unserialized = jsonSerialization.unserializeToNode(serialized);
        assertEquals(Arrays.asList(node), unserialized);
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

    private List<JsonObject> getNodesByConcept(JsonArray nodes, String conceptID) {
        return nodes.asList().stream().map(JsonElement::getAsJsonObject).filter(e -> e.get("concept").getAsString().equals(conceptID)).collect(Collectors.toList());
    }

}
