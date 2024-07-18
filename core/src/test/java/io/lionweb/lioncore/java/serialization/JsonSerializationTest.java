package io.lionweb.lioncore.java.serialization;

import static io.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;
import static org.junit.Assert.*;

import com.google.gson.*;
import io.lionweb.lioncore.java.api.UnresolvedClassifierInstanceException;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.*;
import io.lionweb.lioncore.java.model.impl.DynamicAnnotationInstance;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.model.impl.EnumerationValueImpl;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

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
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
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
    assertEquals(Arrays.asList(explorerBook), ClassifierInstanceUtils.getChildren(bobsLibrary));

    // The library MM is not using the standard primitive types but its own, so we need to specify
    // how to serialize
    // those values
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
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
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
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
  public void deserializeLanguageWithEnumerations() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/TestLang-language.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    List<Node> deserializedNodes = jsonSerialization.deserializeToNodes(jsonElement);

    Enumeration testEnumeration1 =
        (Enumeration)
            deserializedNodes.stream()
                .filter(
                    n ->
                        "MDhjYWFkNzUtODI0Ni00NDI3LWJiNGQtODQ0NGI2YzVjNzI5LzI1ODUzNzgxNjU5NzMyMDQ1ODI"
                            .equals(n.getID()))
                .findFirst()
                .get();
    assertEquals("TestEnumeration1", testEnumeration1.getName());
    assertEquals(2, testEnumeration1.getLiterals().size());

    Concept sideTransformInfo =
        (Concept)
            deserializedNodes.stream()
                .filter(
                    n ->
                        "Y2VhYjUxOTUtMjVlYS00ZjIyLTliOTItMTAzYjk1Y2E4YzBjLzc3OTEyODQ5Mjg1MzM2OTE2NQ"
                            .equals(n.getID()))
                .findFirst()
                .get();
    assertEquals("SideTransformInfo", sideTransformInfo.getName());
    assertEquals(false, sideTransformInfo.isAbstract());
    assertEquals(3, sideTransformInfo.getFeatures().size());
    assertEquals(3, ClassifierInstanceUtils.getChildren(sideTransformInfo).size());
  }

  @Test
  public void deserializeLanguageWithDependencies() {
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    Language starlasu =
        (Language)
            jsonSerialization
                .deserializeToNodes(
                    this.getClass().getResourceAsStream("/properties-example/starlasu.lmm.json"))
                .get(0);
    jsonSerialization.getInstanceResolver().addTree(starlasu);
    Language properties =
        (Language)
            jsonSerialization
                .deserializeToNodes(
                    this.getClass().getResourceAsStream("/properties-example/properties.lmm.json"))
                .get(0);
    LanguageValidator.ensureIsValid(starlasu);
    LanguageValidator.ensureIsValid(properties);
  }

  private void prepareDeserializationOfSimpleMath(JsonSerialization js) {
    js.getClassifierResolver().registerLanguage(SimpleMathLanguage.INSTANCE);
    js.getInstantiator()
        .registerCustomDeserializer(
            SimpleMathLanguage.INT_LITERAL.getID(),
            (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
                new IntLiteral(
                    (Integer) propertiesValues.get(concept.getPropertyByName("value")),
                    serializedNode.getID()));
    js.getInstantiator()
        .registerCustomDeserializer(
            SimpleMathLanguage.SUM.getID(),
            (concept, serializedNode, deserializedNodesByID, propertiesValues) -> {
              SerializedContainmentValue leftSCV =
                  serializedNode.getContainments().stream()
                      .filter(c -> c.getMetaPointer().getKey().equals("SimpleMath_Sum_left"))
                      .findFirst()
                      .get();
              IntLiteral left = (IntLiteral) deserializedNodesByID.get(leftSCV.getValue().get(0));
              SerializedContainmentValue rightSCV =
                  serializedNode.getContainments().stream()
                      .filter(c -> c.getMetaPointer().getKey().equals("SimpleMath_Sum_right"))
                      .findFirst()
                      .get();
              IntLiteral right = (IntLiteral) deserializedNodesByID.get(rightSCV.getValue().get(0));
              return new Sum(left, right, serializedNode.getID());
            });
  }

  @Test
  public void deserializeMultipleRoots() {
    Sum sum1 = new Sum(new IntLiteral(1), new IntLiteral(2));
    Sum sum2 = new Sum(new IntLiteral(3), new IntLiteral(4));
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    JsonElement serialized = js.serializeTreesToJsonElement(sum1, sum2);
    assertEquals(2, serialized.getAsJsonObject().get("languages").getAsJsonArray().size());
    assertEquals(6, serialized.getAsJsonObject().get("nodes").getAsJsonArray().size());
    prepareDeserializationOfSimpleMath(js);
    List<Sum> deserialized =
        js.deserializeToNodes(serialized).stream()
            .filter(n -> n instanceof Sum)
            .map(n -> (Sum) n)
            .collect(Collectors.toList());
    assertEquals(Arrays.asList(sum1, sum2), deserialized);
  }

  @Test
  public void deserializeNodesWithoutIDsInTheRightOrder() {
    IntLiteral il1 = new IntLiteral(1, null);
    IntLiteral il2 = new IntLiteral(2, null);
    IntLiteral il3 = new IntLiteral(3, null);
    IntLiteral il4 = new IntLiteral(4, null);
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    JsonElement serialized = js.serializeTreesToJsonElement(il1, il2, il3, il4);
    prepareDeserializationOfSimpleMath(js);
    List<IntLiteral> deserialized =
        js.deserializeToNodes(serialized).stream()
            .map(n -> (IntLiteral) n)
            .collect(Collectors.toList());
    assertEquals(Arrays.asList(il1, il2, il3, il4), deserialized);
  }

  @Test
  public void deserializeTreesWithoutIDsInTheRightOrder() {
    IntLiteral il1 = new IntLiteral(1, "int_1");
    IntLiteral il2 = new IntLiteral(2, "int_2");
    Sum sum1 = new Sum(il1, il2, null);
    IntLiteral il3 = new IntLiteral(3, "int_3");
    IntLiteral il4 = new IntLiteral(4, "int_4");
    Sum sum2 = new Sum(il3, il4, null);
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    JsonElement serialized = js.serializeTreesToJsonElement(sum1, sum2);
    prepareDeserializationOfSimpleMath(js);
    List<Node> deserialized = js.deserializeToNodes(serialized);
    assertEquals(Arrays.asList(sum1, il1, il2, sum2, il3, il4), deserialized);
  }

  @Test
  public void deserializeTreesWithArbitraryOrderAndNullIDsInTheRightOrder() {
    // handling multiple parents with null IDs require special care as they
    // are ambiguous (i.e., they cannot be distinguished by looking at their ID)
    IntLiteral il1 = new IntLiteral(1, "int_1");
    IntLiteral il2 = new IntLiteral(2, "int_2");
    Sum sum1 = new Sum(il1, il2, null);
    IntLiteral il3 = new IntLiteral(3, "int_3");
    IntLiteral il4 = new IntLiteral(4, "int_4");
    Sum sum2 = new Sum(il3, il4, null);
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    JsonElement serialized = js.serializeNodesToJsonElement(il4, il1, sum1, il2, sum2, il3);
    prepareDeserializationOfSimpleMath(js);
    List<Node> deserialized = js.deserializeToNodes(serialized);
    assertEquals(Arrays.asList(il4, il1, sum1, il2, sum2, il3), deserialized);
  }

  // We should get a DeserializationException as we are unable to reassign the child with null ID
  @Test(expected = DeserializationException.class)
  public void deserializeChildrenWithNullID() {
    IntLiteral il1 = new IntLiteral(1, "int_1");
    IntLiteral il2 = new IntLiteral(2, null);
    Sum sum1 = new Sum(il1, il2, null);
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    JsonElement serialized = js.serializeNodesToJsonElement(sum1, il1, il2);
    prepareDeserializationOfSimpleMath(js);
    List<Node> deserialized = js.deserializeToNodes(serialized);
    assertEquals(Arrays.asList(sum1, il1, il2), deserialized);
  }

  private void prepareDeserializationOfRefMM(JsonSerialization js) {
    js.getClassifierResolver().registerLanguage(RefsLanguage.INSTANCE);
    js.getInstantiator()
        .registerCustomDeserializer(
            RefsLanguage.CONTAINER_NODE.getID(),
            (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
                new ContainerNode(
                    (ContainerNode) propertiesValues.get(concept.getContainmentByName("contained")),
                    serializedNode.getID()));
    js.getInstantiator()
        .registerCustomDeserializer(
            RefsLanguage.REF_NODE.getID(),
            (concept, serializedNode, deserializedNodesByID, propertiesValues) -> {
              return new RefNode(serializedNode.getID());
            });
  }

  @Test(expected = DeserializationException.class)
  public void deadReferences() {
    RefNode r1 = new RefNode();
    RefNode r2 = new RefNode();
    r1.setReferred(r2);
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    JsonElement serialized = js.serializeNodesToJsonElement(r1);
    prepareDeserializationOfRefMM(js);
    List<Node> deserialized = js.deserializeToNodes(serialized);
  }

  @Test
  public void referencesLoop() {
    RefNode r1 = new RefNode();
    RefNode r2 = new RefNode();
    RefNode r3 = new RefNode();
    r1.setReferred(r2);
    r2.setReferred(r3);
    r3.setReferred(r1);
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    JsonElement serialized = js.serializeNodesToJsonElement(r1, r2, r3);
    prepareDeserializationOfRefMM(js);
    List<Node> deserialized = js.deserializeToNodes(serialized);
    assertEquals(Arrays.asList(r1, r2, r3), deserialized);
  }

  @Test(expected = DeserializationException.class)
  public void containmentsLoop() {
    ContainerNode c1 = new ContainerNode();
    ContainerNode c2 = new ContainerNode();
    c1.setContained(c2);
    c2.setContained(c1);
    c2.setParent(c1);
    c1.setParent(c2);

    assertEquals(c2, c1.getParent());
    assertEquals(c1, c2.getParent());
    Assert.assertEquals(Arrays.asList(c2), ClassifierInstanceUtils.getChildren(c1));
    Assert.assertEquals(Arrays.asList(c1), ClassifierInstanceUtils.getChildren(c2));

    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    JsonElement serialized = js.serializeNodesToJsonElement(c1, c2);
    prepareDeserializationOfRefMM(js);
    List<Node> deserialized = js.deserializeToNodes(serialized);
  }

  @Test(expected = DeserializationException.class)
  public void deserializeTreeWithoutRoot() {
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    List<Node> nodes =
        js.deserializeToNodes(
            this.getClass().getResourceAsStream("/mpsMeetup-issue10/example1.json"));
  }

  @Test
  public void serializationOfEnumLiteralUsingEnumerationValueInstances() {
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
    n1.setPropertyValue(p, new EnumerationValueImpl(el1));
    DynamicNode n2 = new DynamicNode("node2", c);
    n2.setPropertyValue(p, new EnumerationValueImpl(el2));
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    js.registerLanguage(mm);

    JsonElement je = js.serializeNodesToJsonElement(n1, n2);
    assertEquals(
        JsonParser.parseString(
            "{\n"
                + "    \"serializationFormatVersion\": \"2023.1\",\n"
                + "    \"languages\": [{\n"
                + "        \"version\": \"1\",\n"
                + "        \"key\": \"mm_key\"\n"
                + "    }],\n"
                + "    \"nodes\": [{\n"
                + "        \"id\": \"node1\",\n"
                + "        \"classifier\": {\n"
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
                + "        \"containments\": [],\n"
                + "        \"references\": [],\n"
                + "        \"annotations\": [],\n"
                + "        \"parent\": null\n"
                + "    }, {\n"
                + "        \"id\": \"node2\",\n"
                + "        \"classifier\": {\n"
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
                + "        \"containments\": [],\n"
                + "        \"references\": [],\n"
                + "        \"annotations\": [],\n"
                + "        \"parent\": null\n"
                + "    }]\n"
                + "}"),
        je);
  }

  enum MyEnum {
    el1,
    el2
  }

  @Test
  public void serializationOfEnumLiteralUsingEnumInstances() {

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
    n1.setPropertyValue(p, MyEnum.el1);
    DynamicNode n2 = new DynamicNode("node2", c);
    n2.setPropertyValue(p, MyEnum.el2);
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    js.registerLanguage(mm);

    JsonElement je = js.serializeNodesToJsonElement(n1, n2);
    assertEquals(
        JsonParser.parseString(
            "{\n"
                + "    \"serializationFormatVersion\": \"2023.1\",\n"
                + "    \"languages\": [{\n"
                + "        \"version\": \"1\",\n"
                + "        \"key\": \"mm_key\"\n"
                + "    }],\n"
                + "    \"nodes\": [{\n"
                + "        \"id\": \"node1\",\n"
                + "        \"classifier\": {\n"
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
                + "        \"containments\": [],\n"
                + "        \"references\": [],\n"
                + "        \"annotations\": [],\n"
                + "        \"parent\": null\n"
                + "    }, {\n"
                + "        \"id\": \"node2\",\n"
                + "        \"classifier\": {\n"
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
                + "        \"containments\": [],\n"
                + "        \"references\": [],\n"
                + "        \"annotations\": [],\n"
                + "        \"parent\": null\n"
                + "    }]\n"
                + "}"),
        je);
  }

  @Test
  public void deserializeEnumerationLiteralsUsingEnumerationValueInstances() {
    JsonElement je =
        JsonParser.parseString(
            "{\n"
                + "    \"serializationFormatVersion\": \"2023.1\",\n"
                + "    \"languages\": [{\n"
                + "        \"version\": \"1\",\n"
                + "        \"key\": \"mm_key\"\n"
                + "    }],\n"
                + "    \"nodes\": [{\n"
                + "        \"id\": \"node1\",\n"
                + "        \"classifier\": {\n"
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
                + "        \"containments\": [],\n"
                + "        \"references\": [],\n"
                + "        \"parent\": null\n"
                + "    }, {\n"
                + "        \"id\": \"node2\",\n"
                + "        \"classifier\": {\n"
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
                + "        \"containments\": [],\n"
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
    n1.setPropertyValue(p, new EnumerationValueImpl(el1));
    DynamicNode n2 = new DynamicNode("node2", c);
    n2.setPropertyValue(p, new EnumerationValueImpl(el2));
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    js.registerLanguage(mm);
    js.getInstantiator().enableDynamicNodes();
    js.getPrimitiveValuesSerialization().enableDynamicNodes();

    List<Node> deserializedNodes = js.deserializeToNodes(je);
    assertEquals(Arrays.asList(n1, n2), deserializedNodes);
    assertEquals(new EnumerationValueImpl(el1), deserializedNodes.get(0).getPropertyValue(p));
    assertEquals(new EnumerationValueImpl(el2), deserializedNodes.get(1).getPropertyValue(p));
  }

  @Test
  public void deserializeEnumerationLiteralsUsingEnumInstances() {
    JsonElement je =
        JsonParser.parseString(
            "{\n"
                + "    \"serializationFormatVersion\": \"2023.1\",\n"
                + "    \"languages\": [{\n"
                + "        \"version\": \"1\",\n"
                + "        \"key\": \"mm_key\"\n"
                + "    }],\n"
                + "    \"nodes\": [{\n"
                + "        \"id\": \"node1\",\n"
                + "        \"classifier\": {\n"
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
                + "        \"containments\": [],\n"
                + "        \"references\": [],\n"
                + "        \"parent\": null\n"
                + "    }, {\n"
                + "        \"id\": \"node2\",\n"
                + "        \"classifier\": {\n"
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
                + "        \"containments\": [],\n"
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
    n1.setPropertyValue(p, MyEnum.el1);
    DynamicNode n2 = new DynamicNode("node2", c);
    n2.setPropertyValue(p, MyEnum.el2);
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    js.getPrimitiveValuesSerialization().registerEnumClass(MyEnum.class, e);
    js.registerLanguage(mm);
    js.getInstantiator().enableDynamicNodes();

    List<Node> deserializedNodes = js.deserializeToNodes(je);
    assertEquals(Arrays.asList(n1, n2), deserializedNodes);
    assertEquals(MyEnum.el1, deserializedNodes.get(0).getPropertyValue(p));
    assertEquals(MyEnum.el2, deserializedNodes.get(1).getPropertyValue(p));
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
    JsonSerialization jsonSer = SerializationProvider.getStandardJsonSerialization();
    SerializedChunk serializedChunk = jsonSer.serializeNodesToSerializationBlock(myInstance);
    assertEquals(1, serializedChunk.getClassifierInstances().size());
    SerializedClassifierInstance serializedClassifierInstance =
        serializedChunk.getClassifierInstances().get(0);
    assertEquals("instance-a", serializedClassifierInstance.getID());
    assertEquals(1, serializedClassifierInstance.getProperties().size());
    SerializedPropertyValue serializedName = serializedClassifierInstance.getProperties().get(0);
    assertEquals(
        new MetaPointer("LionCore-builtins", "2023.1", "LionCore-builtins-INamed-name"),
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

    JsonSerialization hjs = SerializationProvider.getStandardJsonSerialization();
    hjs.enableDynamicNodes();
    SerializedChunk serializedChunk = hjs.serializeNodesToSerializationBlock(n1);

    assertEquals(4, serializedChunk.getClassifierInstances().size());
    SerializedClassifierInstance serializedN1 = serializedChunk.getClassifierInstances().get(0);
    assertEquals("n1", serializedN1.getID());
    assertNull(serializedN1.getParentNodeID());
    assertEquals(Arrays.asList("a1_1", "a1_2", "a2_3"), serializedN1.getAnnotations());
    SerializedClassifierInstance serializedA1_1 = serializedChunk.getClassifierInstances().get(1);
    assertEquals("n1", serializedA1_1.getParentNodeID());

    List<ClassifierInstance<?>> deserialized = hjs.deserializeSerializationBlock(serializedChunk);
    assertEquals(4, deserialized.size());
    assertInstancesAreEquals(a1_1, deserialized.get(1));
    assertEquals(deserialized.get(0), deserialized.get(1).getParent());
    assertInstancesAreEquals(a1_2, deserialized.get(2));
    assertEquals(deserialized.get(0), deserialized.get(2).getParent());
    assertInstancesAreEquals(a2_3, deserialized.get(3));
    assertEquals(deserialized.get(0), deserialized.get(3).getParent());
    assertInstancesAreEquals(n1, deserialized.get(0));
    assertEquals(
        Arrays.asList(deserialized.get(1), deserialized.get(2), deserialized.get(3)),
        deserialized.get(0).getAnnotations());
  }

  @Test
  public void serializeLanguage() {
    Language metaLang = new Language("metaLang", "metaLang", "metaLang", "1");
    Annotation metaAnn = new Annotation(metaLang, "metaAnn", "metaAnn", "metaAnn");

    Language l = new Language("l", "l", "l", "1");
    Annotation a1 = new Annotation(l, "a1", "a1", "a1");
    Annotation a2 = new Annotation(l, "a2", "a2", "a2");
    Concept c = new Concept(l, "c", "c", "c");
    DynamicAnnotationInstance ann = new DynamicAnnotationInstance("metaAnn_1", metaAnn, c);
    c.addAnnotation(ann);

    JsonSerialization hjs = SerializationProvider.getStandardJsonSerialization();
    hjs.enableDynamicNodes();
    SerializedChunk serializedChunk = hjs.serializeTreeToSerializationBlock(l);

    assertEquals(5, serializedChunk.getClassifierInstances().size());
    SerializedClassifierInstance serializedL = serializedChunk.getClassifierInstances().get(0);
    assertEquals("l", serializedL.getID());
    assertNull(serializedL.getParentNodeID());

    SerializedClassifierInstance serializedC = serializedChunk.getInstanceByID("c");
    assertEquals("c", serializedC.getID());
    assertEquals(Arrays.asList("metaAnn_1"), serializedC.getAnnotations());

    hjs.registerLanguage(metaLang);
    List<ClassifierInstance<?>> deserialized = hjs.deserializeSerializationBlock(serializedChunk);
    assertEquals(5, deserialized.size());
    ClassifierInstance<?> deserializedC = deserialized.get(3);
    assertInstancesAreEquals(c, deserializedC);
    assertEquals(deserialized.get(0), deserializedC.getParent());
    ClassifierInstance<?> deserializedAnn = deserialized.get(4);
    assertInstancesAreEquals(ann, deserializedAnn);
    assertEquals(deserializedC, deserializedAnn.getParent());
    assertEquals(Arrays.asList(deserializedAnn), deserializedC.getAnnotations());
  }

  @Test
  public void serializationIncludeBuiltinsWhenUsedInProperties() {
    Language l = new Language("l", "l", "l", "1");
    Concept c = new Concept(l, "c", "c", "c");
    c.addFeature(Property.createRequired("foo", LionCoreBuiltins.getString()));

    DynamicNode n1 = new DynamicNode("n1", c);
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", "abc");

    JsonSerialization hjs = SerializationProvider.getStandardJsonSerialization();
    SerializedChunk serializedChunk = hjs.serializeNodesToSerializationBlock(n1);

    assertEquals(2, serializedChunk.getLanguages().size());
    assertSerializedChunkContainsLanguage(serializedChunk, l);
    assertSerializedChunkContainsLanguage(serializedChunk, LionCoreBuiltins.getInstance());
  }

  private void assertSerializedChunkContainsLanguage(
      SerializedChunk serializedChunk, Language language) {
    assertTrue(
        serializedChunk.getLanguages().stream()
            .anyMatch(
                entry ->
                    entry.getKey().equals(language.getKey())
                        && entry.getVersion().equals(language.getVersion())));
  }

  @Test(expected = DeserializationException.class)
  public void deserializePartialTreeFailsByDefault() {
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    InputStream languageIs =
        this.getClass().getResourceAsStream("/serialization/propertiesLanguage.json");
    Language propertiesLanguage = (Language) js.deserializeToNodes(languageIs).get(0);
    js.registerLanguage(propertiesLanguage);
    InputStream is = this.getClass().getResourceAsStream("/serialization/partialTree.json");

    js.enableDynamicNodes();
    List<Node> nodes = js.deserializeToNodes(is);
  }

  @Test
  public void deserializePartialTreeSucceedsWithNullReferencesUnavailableNodePolicy() {
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    InputStream languageIs =
        this.getClass().getResourceAsStream("/serialization/propertiesLanguage.json");
    Language propertiesLanguage = (Language) js.deserializeToNodes(languageIs).get(0);
    js.registerLanguage(propertiesLanguage);
    InputStream is = this.getClass().getResourceAsStream("/serialization/partialTree.json");

    js.enableDynamicNodes();
    js.setUnavailableParentPolicy(UnavailableNodePolicy.NULL_REFERENCES);
    List<Node> nodes = js.deserializeToNodes(is);
    assertEquals(4, nodes.size());
  }

  @Test
  public void deserializePartialTreeSucceedsWithProxyNodesUnavailableNodePolicy() {
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    InputStream languageIs =
        this.getClass().getResourceAsStream("/serialization/propertiesLanguage.json");
    Language propertiesLanguage = (Language) js.deserializeToNodes(languageIs).get(0);
    js.registerLanguage(propertiesLanguage);
    InputStream is = this.getClass().getResourceAsStream("/serialization/partialTree.json");

    js.enableDynamicNodes();
    js.setUnavailableParentPolicy(UnavailableNodePolicy.PROXY_NODES);
    List<Node> nodes = js.deserializeToNodes(is);
    assertEquals(5, nodes.size());

    Node pp1 = nodes.stream().filter(n -> n.getID().equals("pp1")).findFirst().get();
    assertEquals(true, pp1 instanceof ProxyNode);

    Node pf1 = nodes.stream().filter(n -> n.getID().equals("pf1")).findFirst().get();
    assertEquals(false, pf1 instanceof ProxyNode);
    assertEquals(pp1, pf1.getParent());

    nodes.stream().filter(n -> n != pp1).allMatch(n -> !(n instanceof ProxyNode));
  }

  @Test(expected = DeserializationException.class)
  public void deserializeTreeWithExternalReferencesWithThrowErrorsUnavailableNodePolicy() {
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    InputStream languageIs =
        this.getClass().getResourceAsStream("/serialization/todosLanguage.json");
    Language todosLanguage = (Language) js.deserializeToNodes(languageIs).get(0);
    js.registerLanguage(todosLanguage);
    InputStream is =
        this.getClass().getResourceAsStream("/serialization/todosWithExternalReferences.json");

    js.enableDynamicNodes();
    js.setUnavailableParentPolicy(UnavailableNodePolicy.NULL_REFERENCES);
    js.setUnavailableReferenceTargetPolicy(UnavailableNodePolicy.THROW_ERROR);
    js.deserializeToNodes(is);
  }

  @Test
  public void deserializeTreeWithExternalReferencesWithProxyNodesUnavailableNodePolicy() {
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    InputStream languageIs =
        this.getClass().getResourceAsStream("/serialization/todosLanguage.json");
    Language todosLanguage = (Language) js.deserializeToNodes(languageIs).get(0);
    js.registerLanguage(todosLanguage);
    InputStream is =
        this.getClass().getResourceAsStream("/serialization/todosWithExternalReferences.json");

    js.enableDynamicNodes();
    js.setUnavailableParentPolicy(UnavailableNodePolicy.NULL_REFERENCES);
    js.setUnavailableReferenceTargetPolicy(UnavailableNodePolicy.PROXY_NODES);
    List<Node> nodes = js.deserializeToNodes(is);
    assertEquals(5, nodes.size());

    Node pr0td1 =
        nodes.stream()
            .filter(n -> n.getID().equals("synthetic_my-wonderful-partition_projects_0_todos_1"))
            .findFirst()
            .get();
    assertTrue(pr0td1 instanceof ProxyNode);
    Node pr1td0 =
        nodes.stream()
            .filter(n -> n.getID().equals("synthetic_my-wonderful-partition_projects_1_todos_0"))
            .findFirst()
            .get();
    assertTrue(pr1td0 instanceof DynamicNode);
    Node pr1td1 =
        nodes.stream()
            .filter(n -> n.getID().equals("synthetic_my-wonderful-partition_projects_1_todos_1"))
            .findFirst()
            .get();
    assertTrue(pr1td1 instanceof DynamicNode);
    Node pr1td2 =
        nodes.stream()
            .filter(n -> n.getID().equals("synthetic_my-wonderful-partition_projects_1_todos_2"))
            .findFirst()
            .get();
    assertTrue(pr1td1 instanceof DynamicNode);

    // local reference
    assertEquals(
        Arrays.asList(new ReferenceValue(pr1td0, "BD")),
        ClassifierInstanceUtils.getReferenceValueByName(pr1td1, "prerequisite"));

    // external reference
    assertEquals(
        Arrays.asList(new ReferenceValue(pr0td1, "garbage-out")),
        ClassifierInstanceUtils.getReferenceValueByName(pr1td2, "prerequisite"));
  }

  @Test
  public void deserializeTreeWithExternalReferencesSetToNullPolicyUnavailableNodePolicy() {
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    InputStream languageIs =
        this.getClass().getResourceAsStream("/serialization/todosLanguage.json");
    Language todosLanguage = (Language) js.deserializeToNodes(languageIs).get(0);
    js.registerLanguage(todosLanguage);
    InputStream is =
        this.getClass().getResourceAsStream("/serialization/todosWithExternalReferences.json");

    js.enableDynamicNodes();
    js.setUnavailableParentPolicy(UnavailableNodePolicy.NULL_REFERENCES);
    js.setUnavailableReferenceTargetPolicy(UnavailableNodePolicy.NULL_REFERENCES);
    List<Node> nodes = js.deserializeToNodes(is);
    assertEquals(4, nodes.size());

    Node pr1td0 =
        nodes.stream()
            .filter(n -> n.getID().equals("synthetic_my-wonderful-partition_projects_1_todos_0"))
            .findFirst()
            .get();
    assertTrue(pr1td0 instanceof DynamicNode);
    Node pr1td1 =
        nodes.stream()
            .filter(n -> n.getID().equals("synthetic_my-wonderful-partition_projects_1_todos_1"))
            .findFirst()
            .get();
    assertTrue(pr1td1 instanceof DynamicNode);
    Node pr1td2 =
        nodes.stream()
            .filter(n -> n.getID().equals("synthetic_my-wonderful-partition_projects_1_todos_2"))
            .findFirst()
            .get();
    assertTrue(pr1td2 instanceof DynamicNode);

    // local reference
    assertEquals(
        Arrays.asList(new ReferenceValue(pr1td0, "BD")),
        ClassifierInstanceUtils.getReferenceValueByName(pr1td1, "prerequisite"));

    // external reference
    assertEquals(
        Arrays.asList(new ReferenceValue(null, "garbage-out")),
        ClassifierInstanceUtils.getReferenceValueByName(pr1td2, "prerequisite"));
  }

  @Test
  public void deserializeTreesWithChildrenNotProvided() {
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    InputStream languageIs =
        this.getClass().getResourceAsStream("/serialization/todosLanguage.json");
    Language todosLanguage = (Language) js.deserializeToNodes(languageIs).get(0);
    js.registerLanguage(todosLanguage);

    js.enableDynamicNodes();
    js.setUnavailableParentPolicy(UnavailableNodePolicy.NULL_REFERENCES);
    js.setUnavailableReferenceTargetPolicy(UnavailableNodePolicy.NULL_REFERENCES);
    assertThrows(
        UnresolvedClassifierInstanceException.class,
        new ThrowingRunnable() {
          @Override
          public void run() throws Throwable {
            InputStream is =
                this.getClass()
                    .getResourceAsStream("/serialization/todosWithChildrenNotProvided.json");
            List<Node> nodes = js.deserializeToNodes(is);
          }
        });

    js.setUnavailableChildrenPolicy(UnavailableNodePolicy.PROXY_NODES);
    InputStream is =
        this.getClass().getResourceAsStream("/serialization/todosWithChildrenNotProvided.json");
    List<Node> nodes = js.deserializeToNodes(is);
    assertEquals(1, nodes.size());

    Node root =
        nodes.stream()
            .filter(n -> n.getID().equals("synthetic_my-wonderful-partition_projects_1"))
            .findFirst()
            .get();
    assertTrue(root instanceof DynamicNode);
    assertEquals(3, ClassifierInstanceUtils.getChildrenByContainmentName(root, "todos").size());
    Node pr1td0 = ClassifierInstanceUtils.getChildrenByContainmentName(root, "todos").get(0);
    assertEquals("synthetic_my-wonderful-partition_projects_1_todos_0", pr1td0.getID());
    assertTrue(pr1td0 instanceof ProxyNode);
    Node pr1td1 = ClassifierInstanceUtils.getChildrenByContainmentName(root, "todos").get(1);
    assertEquals("synthetic_my-wonderful-partition_projects_1_todos_1", pr1td1.getID());
    assertTrue(pr1td1 instanceof ProxyNode);
    Node pr1td2 = ClassifierInstanceUtils.getChildrenByContainmentName(root, "todos").get(2);
    assertEquals("synthetic_my-wonderful-partition_projects_1_todos_2", pr1td2.getID());
    assertTrue(pr1td2 instanceof ProxyNode);
  }

  @Test
  public void deserializeMultipleReferencesToProxiedNode() {
    JsonSerialization js = SerializationProvider.getStandardJsonSerialization();
    InputStream languageIs =
        this.getClass().getResourceAsStream("/serialization/todosLanguage.json");
    Language todosLanguage = (Language) js.deserializeToNodes(languageIs).get(0);
    js.registerLanguage(todosLanguage);

    js.enableDynamicNodes();
    js.setUnavailableChildrenPolicy(UnavailableNodePolicy.PROXY_NODES);
    js.setUnavailableParentPolicy(UnavailableNodePolicy.PROXY_NODES);
    js.setUnavailableReferenceTargetPolicy(UnavailableNodePolicy.PROXY_NODES);
    InputStream is =
        this.getClass().getResourceAsStream("/serialization/todosWithMultipleProxies.json");
    List<Node> nodes = js.deserializeToNodes(is);
    assertEquals(5, nodes.size());

    Node todo0 = nodes.get(0);
    assertEquals(new ProxyNode("synthetic_my-wonderful-partition_projects_1"), todo0.getParent());
    List<ReferenceValue> prerequisiteTodo0 =
        ClassifierInstanceUtils.getReferenceValueByName(todo0, "prerequisite");
    assertEquals(
        Arrays.asList(new ReferenceValue(new ProxyNode("external-1"), null)), prerequisiteTodo0);

    Node todo1 = nodes.get(1);
    assertEquals(new ProxyNode("synthetic_my-wonderful-partition_projects_1"), todo1.getParent());
    List<ReferenceValue> prerequisiteTodo1 =
        ClassifierInstanceUtils.getReferenceValueByName(todo1, "prerequisite");
    assertEquals(
        Arrays.asList(new ReferenceValue(new ProxyNode("external-1"), null)), prerequisiteTodo1);

    Node todo2 = nodes.get(2);
    assertEquals(new ProxyNode("synthetic_my-wonderful-partition_projects_1"), todo2.getParent());
    List<ReferenceValue> prerequisiteTodo2 =
        ClassifierInstanceUtils.getReferenceValueByName(todo2, "prerequisite");
    assertEquals(
        Arrays.asList(new ReferenceValue(new ProxyNode("external-1"), null)), prerequisiteTodo2);

    assertTrue(nodes.get(3) instanceof ProxyNode);
    assertTrue(nodes.get(4) instanceof ProxyNode);
    ProxyNode n3 = (ProxyNode) nodes.get(3);
    ProxyNode n4 = (ProxyNode) nodes.get(4);
    assertEquals(
        new HashSet(Arrays.asList("synthetic_my-wonderful-partition_projects_1", "external-1")),
        new HashSet(Arrays.asList(n3.getID(), n4.getID())));
  }
}
