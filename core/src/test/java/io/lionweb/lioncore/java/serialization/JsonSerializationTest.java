package io.lionweb.lioncore.java.serialization;

import static io.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;
import static org.junit.Assert.assertEquals;

import com.google.gson.*;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Enumeration;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.model.impl.DynamicAnnotationInstance;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.lioncore.java.serialization.refsmm.ContainerNode;
import io.lionweb.lioncore.java.serialization.refsmm.RefNode;
import io.lionweb.lioncore.java.serialization.refsmm.RefsLanguage;
import io.lionweb.lioncore.java.serialization.simplemath.IntLiteral;
import io.lionweb.lioncore.java.serialization.simplemath.SimpleMathLanguage;
import io.lionweb.lioncore.java.serialization.simplemath.Sum;
import io.lionweb.lioncore.java.utils.LanguageValidator;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

/** Testing various functionalities of JsonSerialization. */
public class JsonSerializationTest extends SerializationTest {

  @Test
  public void serializeReferenceWithoutResolveInfo() {
    Node book = new DynamicNode("foo123", LibraryLanguage.BOOK);
    Node writer = new DynamicNode("-Arthur-Foozillus-id-", LibraryLanguage.WRITER);
    book.addReferenceValue(
        LibraryLanguage.BOOK.getReferenceByName("author"), new ReferenceValue(writer, null));

    // The library MM is not using the standard primitive types but its own, so we need to specify
    // how to serialize
    // those values
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerSerializer(
            "INhBvWyXvxwNsePuX0rdNGB_J9hi85cTb1Q0APXCyJ0",
            (PrimitiveValuesSerialization.PrimitiveSerializer<String>) value -> value);
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerSerializer(
            "gVp8_QSmXE2k4pd-sQZgjYMoW95SLLaVIH4yMYqqbt4",
            (PrimitiveValuesSerialization.PrimitiveSerializer<Integer>) value -> value.toString());
    JsonObject jsonSerialized =
        jsonSerialization.serializeNodesToJsonElement(book).getAsJsonObject();
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/foo-library.json");
    JsonObject jsonRead =
        JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
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

    // The library MM is not using the standard primitive types but its own, so we need to specify
    // how to serialize
    // those values
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerSerializer(
            "INhBvWyXvxwNsePuX0rdNGB_J9hi85cTb1Q0APXCyJ0",
            (PrimitiveValuesSerialization.PrimitiveSerializer<String>) value -> value);
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerSerializer(
            "gVp8_QSmXE2k4pd-sQZgjYMoW95SLLaVIH4yMYqqbt4",
            (PrimitiveValuesSerialization.PrimitiveSerializer<Integer>) value -> value.toString());
    JsonObject jsonSerialized =
        jsonSerialization.serializeTreesToJsonElement(bobsLibrary, jackLondon).getAsJsonObject();
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/bobslibrary.json");
    JsonObject jsonRead =
        JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
    assertEquivalentLionWebJson(jsonRead, jsonSerialized);
  }

  @Test
  public void serializeMultipleSubtreesSkipDuplicateNodes() {
    Library bobsLibrary = new Library("bl", "Bob's Library");
    GuideBookWriter jackLondon = new GuideBookWriter("jl", "Jack London");
    jackLondon.setCountries("Alaska");
    Book explorerBook = new Book("eb", "Explorer Book", jackLondon);
    bobsLibrary.addBook(explorerBook);

    // The library MM is not using the standard primitive types but its own, so we need to specify
    // how to serialize
    // those values
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerSerializer(
            "INhBvWyXvxwNsePuX0rdNGB_J9hi85cTb1Q0APXCyJ0",
            (PrimitiveValuesSerialization.PrimitiveSerializer<String>) value -> value);
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerSerializer(
            "gVp8_QSmXE2k4pd-sQZgjYMoW95SLLaVIH4yMYqqbt4",
            (PrimitiveValuesSerialization.PrimitiveSerializer<Integer>) value -> value.toString());
    JsonObject jsonSerialized =
        jsonSerialization
            .serializeNodesToJsonElement(bobsLibrary, jackLondon, explorerBook)
            .getAsJsonObject();
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/bobslibrary.json");
    JsonObject jsonRead =
        JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
    assertEquivalentLionWebJson(jsonRead, jsonSerialized);
  }

  @Test
  public void unserializeLanguageWithEnumerations() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/TestLang-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNodes(jsonElement);

    Enumeration testEnumeration1 =
        (Enumeration)
            unserializedNodes.stream()
                .filter(
                    n ->
                        n.getID()
                            .equals(
                                "MDhjYWFkNzUtODI0Ni00NDI3LWJiNGQtODQ0NGI2YzVjNzI5LzI1ODUzNzgxNjU5NzMyMDQ1ODI"))
                .findFirst()
                .get();
    assertEquals("TestEnumeration1", testEnumeration1.getName());
    assertEquals(2, testEnumeration1.getLiterals().size());

    Concept sideTransformInfo =
        (Concept)
            unserializedNodes.stream()
                .filter(
                    n ->
                        n.getID()
                            .equals(
                                "Y2VhYjUxOTUtMjVlYS00ZjIyLTliOTItMTAzYjk1Y2E4YzBjLzc3OTEyODQ5Mjg1MzM2OTE2NQ"))
                .findFirst()
                .get();
    assertEquals("SideTransformInfo", sideTransformInfo.getName());
    assertEquals(false, sideTransformInfo.isAbstract());
    assertEquals(3, sideTransformInfo.getFeatures().size());
    assertEquals(3, sideTransformInfo.getChildren().size());
  }

  @Test
  public void unserializeLanguageWithDependencies() {
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    Language starlasu =
        (Language)
            jsonSerialization
                .unserializeToNodes(
                    this.getClass().getResourceAsStream("/properties-example/starlasu.lmm.json"))
                .get(0);
    jsonSerialization.getInstanceResolver().addTree(starlasu);
    Language properties =
        (Language)
            jsonSerialization
                .unserializeToNodes(
                    this.getClass().getResourceAsStream("/properties-example/properties.lmm.json"))
                .get(0);
    LanguageValidator.ensureIsValid(starlasu);
    LanguageValidator.ensureIsValid(properties);
  }

  private void prepareUnserializationOfSimpleMath(JsonSerialization js) {
    js.getClassifierResolver().registerLanguage(SimpleMathLanguage.INSTANCE);
    js.getInstantiator()
        .registerCustomUnserializer(
            SimpleMathLanguage.INT_LITERAL.getID(),
            (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
                new IntLiteral(
                    (Integer) propertiesValues.get(concept.getPropertyByName("value")),
                    serializedNode.getID()));
    js.getInstantiator()
        .registerCustomUnserializer(
            SimpleMathLanguage.SUM.getID(),
            (concept, serializedNode, unserializedNodesByID, propertiesValues) -> {
              SerializedContainmentValue leftSCV =
                  serializedNode.getContainments().stream()
                      .filter(c -> c.getMetaPointer().getKey().equals("SimpleMath_Sum_left"))
                      .findFirst()
                      .get();
              IntLiteral left = (IntLiteral) unserializedNodesByID.get(leftSCV.getValue().get(0));
              SerializedContainmentValue rightSCV =
                  serializedNode.getContainments().stream()
                      .filter(c -> c.getMetaPointer().getKey().equals("SimpleMath_Sum_right"))
                      .findFirst()
                      .get();
              IntLiteral right = (IntLiteral) unserializedNodesByID.get(rightSCV.getValue().get(0));
              return new Sum(left, right, serializedNode.getID());
            });
  }

  @Test
  public void unserializeMultipleRoots() {
    Sum sum1 = new Sum(new IntLiteral(1), new IntLiteral(2));
    Sum sum2 = new Sum(new IntLiteral(3), new IntLiteral(4));
    JsonSerialization js = JsonSerialization.getStandardSerialization();
    JsonElement serialized = js.serializeTreesToJsonElement(sum1, sum2);
    assertEquals(1, serialized.getAsJsonObject().get("languages").getAsJsonArray().size());
    assertEquals(6, serialized.getAsJsonObject().get("nodes").getAsJsonArray().size());
    prepareUnserializationOfSimpleMath(js);
    List<Sum> unserialized =
        js.unserializeToNodes(serialized).stream()
            .filter(n -> n instanceof Sum)
            .map(n -> (Sum) n)
            .collect(Collectors.toList());
    assertEquals(Arrays.asList(sum1, sum2), unserialized);
  }

  @Test
  public void unserializeNodesWithoutIDsInTheRightOrder() {
    IntLiteral il1 = new IntLiteral(1, null);
    IntLiteral il2 = new IntLiteral(2, null);
    IntLiteral il3 = new IntLiteral(3, null);
    IntLiteral il4 = new IntLiteral(4, null);
    JsonSerialization js = JsonSerialization.getStandardSerialization();
    JsonElement serialized = js.serializeTreesToJsonElement(il1, il2, il3, il4);
    prepareUnserializationOfSimpleMath(js);
    List<IntLiteral> unserialized =
        js.unserializeToNodes(serialized).stream()
            .map(n -> (IntLiteral) n)
            .collect(Collectors.toList());
    assertEquals(Arrays.asList(il1, il2, il3, il4), unserialized);
  }

  @Test
  public void unserializeTreesWithoutIDsInTheRightOrder() {
    IntLiteral il1 = new IntLiteral(1, "int_1");
    IntLiteral il2 = new IntLiteral(2, "int_2");
    Sum sum1 = new Sum(il1, il2, null);
    IntLiteral il3 = new IntLiteral(3, "int_3");
    IntLiteral il4 = new IntLiteral(4, "int_4");
    Sum sum2 = new Sum(il3, il4, null);
    JsonSerialization js = JsonSerialization.getStandardSerialization();
    JsonElement serialized = js.serializeTreesToJsonElement(sum1, sum2);
    prepareUnserializationOfSimpleMath(js);
    List<Node> unserialized = js.unserializeToNodes(serialized);
    assertEquals(Arrays.asList(sum1, il1, il2, sum2, il3, il4), unserialized);
  }

  @Test
  public void unserializeTreesWithArbitraryOrderAndNullIDsInTheRightOrder() {
    // handling multiple parents with null IDs require special care as they
    // are ambiguous (i.e., they cannot be distinguished by looking at their ID)
    IntLiteral il1 = new IntLiteral(1, "int_1");
    IntLiteral il2 = new IntLiteral(2, "int_2");
    Sum sum1 = new Sum(il1, il2, null);
    IntLiteral il3 = new IntLiteral(3, "int_3");
    IntLiteral il4 = new IntLiteral(4, "int_4");
    Sum sum2 = new Sum(il3, il4, null);
    JsonSerialization js = JsonSerialization.getStandardSerialization();
    JsonElement serialized = js.serializeNodesToJsonElement(il4, il1, sum1, il2, sum2, il3);
    prepareUnserializationOfSimpleMath(js);
    List<Node> unserialized = js.unserializeToNodes(serialized);
    assertEquals(Arrays.asList(il4, il1, sum1, il2, sum2, il3), unserialized);
  }

  // We should get a UnserializationException as we are unable to reassign the child with null ID
  @Test(expected = UnserializationException.class)
  public void deserializeChildrenWithNullID() {
    IntLiteral il1 = new IntLiteral(1, "int_1");
    IntLiteral il2 = new IntLiteral(2, null);
    Sum sum1 = new Sum(il1, il2, null);
    JsonSerialization js = JsonSerialization.getStandardSerialization();
    JsonElement serialized = js.serializeNodesToJsonElement(sum1, il1, il2);
    prepareUnserializationOfSimpleMath(js);
    List<Node> unserialized = js.unserializeToNodes(serialized);
    assertEquals(Arrays.asList(sum1, il1, il2), unserialized);
  }

  private void prepareUnserializationOfRefMM(JsonSerialization js) {
    js.getClassifierResolver().registerLanguage(RefsLanguage.INSTANCE);
    js.getInstantiator()
        .registerCustomUnserializer(
            RefsLanguage.CONTAINER_NODE.getID(),
            (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
                new ContainerNode(
                    (ContainerNode) propertiesValues.get(concept.getContainmentByName("contained")),
                    serializedNode.getID()));
    js.getInstantiator()
        .registerCustomUnserializer(
            RefsLanguage.REF_NODE.getID(),
            (concept, serializedNode, unserializedNodesByID, propertiesValues) -> {
              return new RefNode(serializedNode.getID());
            });
  }

  @Test(expected = UnserializationException.class)
  public void deadReferences() {
    RefNode r1 = new RefNode();
    RefNode r2 = new RefNode();
    r1.setReferred(r2);
    JsonSerialization js = JsonSerialization.getStandardSerialization();
    JsonElement serialized = js.serializeNodesToJsonElement(r1);
    prepareUnserializationOfRefMM(js);
    List<Node> unserialized = js.unserializeToNodes(serialized);
  }

  @Test
  public void referencesLoop() {
    RefNode r1 = new RefNode();
    RefNode r2 = new RefNode();
    RefNode r3 = new RefNode();
    r1.setReferred(r2);
    r2.setReferred(r3);
    r3.setReferred(r1);
    JsonSerialization js = JsonSerialization.getStandardSerialization();
    JsonElement serialized = js.serializeNodesToJsonElement(r1, r2, r3);
    prepareUnserializationOfRefMM(js);
    List<Node> unserialized = js.unserializeToNodes(serialized);
    assertEquals(Arrays.asList(r1, r2, r3), unserialized);
  }

  @Test(expected = UnserializationException.class)
  public void containmentsLoop() {
    ContainerNode c1 = new ContainerNode();
    ContainerNode c2 = new ContainerNode();
    c1.setContained(c2);
    c2.setContained(c1);
    c2.setParent(c1);
    c1.setParent(c2);

    assertEquals(c2, c1.getParent());
    assertEquals(c1, c2.getParent());
    Assert.assertEquals(Arrays.asList(c2), c1.getChildren());
    Assert.assertEquals(Arrays.asList(c1), c2.getChildren());

    JsonSerialization js = JsonSerialization.getStandardSerialization();
    JsonElement serialized = js.serializeNodesToJsonElement(c1, c2);
    prepareUnserializationOfRefMM(js);
    List<Node> unserialized = js.unserializeToNodes(serialized);
  }

  @Test(expected = UnserializationException.class)
  public void unserializeTreeWithoutRoot() {
    JsonSerialization js = JsonSerialization.getStandardSerialization();
    List<Node> nodes =
        js.unserializeToNodes(
            this.getClass().getResourceAsStream("/mpsMeetup-issue10/example1.json"));
  }

  @Test
  public void serializationOfEnumLiteral() {
    Language mm = new Language("my.language").setID("mm_id").setKey("mm_key").setVersion("1");

    Enumeration e =
        new Enumeration(mm, "my.enumeration").setID("enumeration_id").setKey("enumeration_key");
    EnumerationLiteral el1 = new EnumerationLiteral(e, "el1").setID("el1_id").setKey("el1_key");
    EnumerationLiteral el2 = new EnumerationLiteral(e, "el2").setID("el2_id").setKey("el2_key");

    Concept c = new Concept(mm, "my.concept").setID("concept_id").setKey("concept_key");
    Property p =
        Property.createRequired("my.property", e).setID("property_id").setKey("property_key");
    c.addFeature(p);
    DynamicNode n1 = new DynamicNode("node1", c);
    n1.setPropertyValue(p, el1);
    DynamicNode n2 = new DynamicNode("node2", c);
    n2.setPropertyValue(p, el2);
    JsonSerialization js = JsonSerialization.getStandardSerialization();
    js.registerLanguage(mm);

    JsonElement je = js.serializeNodesToJsonElement(n1, n2);
    assertEquals(
        JsonParser.parseString(
            "{\n"
                + "    \"serializationFormatVersion\": \"1\",\n"
                + "    \"languages\": [{\n"
                + "        \"version\": \"1\",\n"
                + "        \"key\": \"mm_key\"\n"
                + "    }],\n"
                + "    \"nodes\": [{\n"
                + "        \"id\": \"node1\",\n"
                + "        \"concept\": {\n"
                + "            \"language\": \"mm_key\",\n"
                + "            \"version\": \"1\",\n"
                + "            \"key\": \"concept_key\"\n"
                + "        },\n"
                + "        \"properties\": [{\n"
                + "            \"property\": {\n"
                + "                \"language\": \"mm_key\",\n"
                + "                \"version\": \"1\",\n"
                + "                \"key\": \"property_key\"\n"
                + "            },\n"
                + "            \"value\": \"el1_key\"\n"
                + "        }],\n"
                + "        \"children\": [],\n"
                + "        \"references\": [],\n"
                + "        \"annotations\": [],\n"
                + "        \"parent\": null\n"
                + "    }, {\n"
                + "        \"id\": \"node2\",\n"
                + "        \"concept\": {\n"
                + "            \"language\": \"mm_key\",\n"
                + "            \"version\": \"1\",\n"
                + "            \"key\": \"concept_key\"\n"
                + "        },\n"
                + "        \"properties\": [{\n"
                + "            \"property\": {\n"
                + "                \"language\": \"mm_key\",\n"
                + "                \"version\": \"1\",\n"
                + "                \"key\": \"property_key\"\n"
                + "            },\n"
                + "            \"value\": \"el2_key\"\n"
                + "        }],\n"
                + "        \"children\": [],\n"
                + "        \"references\": [],\n"
                + "        \"annotations\": [],\n"
                + "        \"parent\": null\n"
                + "    }]\n"
                + "}"),
        je);
  }

  @Test
  public void unserializeEnumerationLiterals() {
    JsonElement je =
        JsonParser.parseString(
            "{\n"
                + "    \"serializationFormatVersion\": \"1\",\n"
                + "    \"languages\": [{\n"
                + "        \"version\": \"1\",\n"
                + "        \"key\": \"mm_key\"\n"
                + "    }],\n"
                + "    \"nodes\": [{\n"
                + "        \"id\": \"node1\",\n"
                + "        \"concept\": {\n"
                + "            \"language\": \"mm_key\",\n"
                + "            \"version\": \"1\",\n"
                + "            \"key\": \"concept_key\"\n"
                + "        },\n"
                + "        \"properties\": [{\n"
                + "            \"property\": {\n"
                + "                \"language\": \"mm_key\",\n"
                + "                \"version\": \"1\",\n"
                + "                \"key\": \"property_key\"\n"
                + "            },\n"
                + "            \"value\": \"el1_key\"\n"
                + "        }],\n"
                + "        \"children\": [],\n"
                + "        \"references\": [],\n"
                + "        \"parent\": null\n"
                + "    }, {\n"
                + "        \"id\": \"node2\",\n"
                + "        \"concept\": {\n"
                + "            \"language\": \"mm_key\",\n"
                + "            \"version\": \"1\",\n"
                + "            \"key\": \"concept_key\"\n"
                + "        },\n"
                + "        \"properties\": [{\n"
                + "            \"property\": {\n"
                + "                \"language\": \"mm_key\",\n"
                + "                \"version\": \"1\",\n"
                + "                \"key\": \"property_key\"\n"
                + "            },\n"
                + "            \"value\": \"el2_key\"\n"
                + "        }],\n"
                + "        \"children\": [],\n"
                + "        \"references\": [],\n"
                + "        \"parent\": null\n"
                + "    }]\n"
                + "}");
    Language mm = new Language("my.language").setID("mm_id").setKey("mm_key").setVersion("1");

    Enumeration e =
        new Enumeration(mm, "my.enumeration").setID("enumeration_id").setKey("enumeration_key");
    EnumerationLiteral el1 = new EnumerationLiteral(e, "el1").setID("el1_id").setKey("el1_key");
    EnumerationLiteral el2 = new EnumerationLiteral(e, "el2").setID("el2_id").setKey("el2_key");

    Concept c = new Concept(mm, "my.concept").setID("concept_id").setKey("concept_key");
    Property p =
        Property.createRequired("my.property", e).setID("property_id").setKey("property_key");
    c.addFeature(p);
    DynamicNode n1 = new DynamicNode("node1", c);
    n1.setPropertyValue(p, el1);
    DynamicNode n2 = new DynamicNode("node2", c);
    n2.setPropertyValue(p, el2);
    JsonSerialization js = JsonSerialization.getStandardSerialization();
    js.registerLanguage(mm);
    js.getInstantiator().enableDynamicNodes();

    List<Node> unserializedNodes = js.unserializeToNodes(je);
    assertEquals(Arrays.asList(n1, n2), unserializedNodes);
    assertEquals(el1, unserializedNodes.get(0).getPropertyValue(p));
    assertEquals(el2, unserializedNodes.get(1).getPropertyValue(p));
  }

  @Test
  public void serializationOfLanguageVersionsWithImports() {
    Language myLanguage = new Language();
    myLanguage.setKey("myLanguage-key");
    myLanguage.setVersion("3");
    Concept myConcept = new Concept();
    myConcept.addImplementedInterface(LionCoreBuiltins.getINamed());
    myLanguage.addElement(myConcept);

    DynamicNode myInstance = new DynamicNode("instance-a", myConcept);
    JsonSerialization jsonSer = JsonSerialization.getStandardSerialization();
    SerializedChunk serializedChunk = jsonSer.serializeNodesToSerializationBlock(myInstance);
    assertEquals(1, serializedChunk.getClassifierInstances().size());
    SerializedClassifierInstance serializedClassifierInstance =
        serializedChunk.getClassifierInstances().get(0);
    assertEquals("instance-a", serializedClassifierInstance.getID());
    assertEquals(1, serializedClassifierInstance.getProperties().size());
    SerializedPropertyValue serializedName = serializedClassifierInstance.getProperties().get(0);
    assertEquals(
        new MetaPointer("LIonCore-builtins", "1", "LIonCore-builtins-INamed-name"),
        serializedName.getMetaPointer());
  }

  @Test
  public void serializeAnnotations() {
    Language l = new Language("l", "l", "l", "1");
    Annotation a1 = new Annotation(l, "a1", "a1", "a1");
    Annotation a2 = new Annotation(l, "a2", "a2", "a2");
    Concept c = new Concept(l, "c", "c", "c");

    DynamicNode n1 = new DynamicNode("n1", c);
    AnnotationInstance a1_1 = new DynamicAnnotationInstance("a1_1", a1, n1);
    AnnotationInstance a1_2 = new DynamicAnnotationInstance("a1_2", a1, n1);
    AnnotationInstance a2_3 = new DynamicAnnotationInstance("a2_3", a2, n1);

    JsonSerialization hjs = JsonSerialization.getStandardSerialization();
    hjs.enableDynamicNodes();
    SerializedChunk serializedChunk = hjs.serializeNodesToSerializationBlock(n1);

    assertEquals(4, serializedChunk.getClassifierInstances().size());
    SerializedNodeInstance serializedN1 =
        (SerializedNodeInstance) serializedChunk.getClassifierInstances().get(0);
    assertEquals("n1", serializedN1.getID());
    assertEquals(null, serializedN1.getParentNodeID());
    assertEquals(Arrays.asList("a1_1", "a1_2", "a2_3"), serializedN1.getAnnotations());
    SerializedAnnotationInstance serializedA1_1 =
        (SerializedAnnotationInstance) serializedChunk.getClassifierInstances().get(1);
    assertEquals("n1", serializedA1_1.getParentNodeID());

    List<ClassifierInstance<?>> unserialized = hjs.unserializeSerializationBlock(serializedChunk);
    assertEquals(4, unserialized.size());
    assertInstancesAreEquals(a1_1, unserialized.get(1));
    assertEquals(unserialized.get(0), unserialized.get(1).getParent());
    assertInstancesAreEquals(a1_2, unserialized.get(2));
    assertEquals(unserialized.get(0), unserialized.get(2).getParent());
    assertInstancesAreEquals(a2_3, unserialized.get(3));
    assertEquals(unserialized.get(0), unserialized.get(3).getParent());
    assertInstancesAreEquals(n1, unserialized.get(0));
    assertEquals(
        Arrays.asList(unserialized.get(1), unserialized.get(2), unserialized.get(3)),
        unserialized.get(0).getAnnotations());
  }
}
