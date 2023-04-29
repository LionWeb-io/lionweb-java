package org.lionweb.lioncore.java.serialization;

import static org.junit.Assert.assertEquals;
import static org.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;

import com.google.gson.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.ReferenceValue;
import org.lionweb.lioncore.java.model.impl.DynamicNode;
import org.lionweb.lioncore.java.serialization.data.SerializedContainmentValue;
import org.lionweb.lioncore.java.serialization.data.SerializedNode;
import org.lionweb.lioncore.java.serialization.simplemath.IntLiteral;
import org.lionweb.lioncore.java.serialization.simplemath.SimpleMathMetamodel;
import org.lionweb.lioncore.java.serialization.simplemath.Sum;
import org.lionweb.lioncore.java.utils.MetamodelValidator;

/** Testing various functionalities of JsonSerialization. */
public class JsonSerializationTest extends SerializationTest {

  @Test
  public void serializeReferenceWithoutResolveInfo() {
    Node book = new DynamicNode("foo123", LibraryMetamodel.BOOK);
    Node writer = new DynamicNode("_Arthur_Foozillus_id_", LibraryMetamodel.WRITER);
    book.addReferenceValue(
        LibraryMetamodel.BOOK.getReferenceByName("author"), new ReferenceValue(writer, null));

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
  public void unserializeMetamodelWithEnumerations() {
    InputStream inputStream =
        this.getClass().getResourceAsStream("/serialization/TestLang-metamodel.json");
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
  public void unserializeMetamodelWithDependencies() {
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    Metamodel starlasu =
        (Metamodel)
            jsonSerialization
                .unserializeToNodes(
                    this.getClass().getResourceAsStream("/properties-example/starlasu.lmm.json"))
                .get(0);
    jsonSerialization.getNodeResolver().addTree(starlasu);
    Metamodel properties =
        (Metamodel)
            jsonSerialization
                .unserializeToNodes(
                    this.getClass().getResourceAsStream("/properties-example/properties.lmm.json"))
                .get(0);
    MetamodelValidator.ensureIsValid(starlasu);
    MetamodelValidator.ensureIsValid(properties);
  }

  private void prepareUnserializationOfSimpleMath(JsonSerialization js) {
    js.getConceptResolver().registerMetamodel(SimpleMathMetamodel.INSTANCE);
    js.getNodeInstantiator().registerCustomUnserializer(SimpleMathMetamodel.INT_LITERAL.getID(),
            (concept, serializedNode, unserializedNodesByID, propertiesValues) ->
                    new IntLiteral((Integer)propertiesValues.get(concept.getPropertyByName("value")), serializedNode.getID()));
    js.getNodeInstantiator().registerCustomUnserializer(SimpleMathMetamodel.SUM.getID(),
            (concept, serializedNode, unserializedNodesByID, propertiesValues) -> {
              SerializedContainmentValue leftSCV = serializedNode.getContainments().stream().filter(c -> c.getMetaPointer().getKey().equals("SimpleMath_Sum_left")).findFirst().get();
              IntLiteral left = (IntLiteral) unserializedNodesByID.get(leftSCV.getValue().get(0));
              SerializedContainmentValue rightSCV = serializedNode.getContainments().stream().filter(c -> c.getMetaPointer().getKey().equals("SimpleMath_Sum_right")).findFirst().get();
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
    assertEquals(1, serialized.getAsJsonObject().get("metamodels").getAsJsonArray().size());
    assertEquals(6, serialized.getAsJsonObject().get("nodes").getAsJsonArray().size());
    prepareUnserializationOfSimpleMath(js);
    List<Sum> unserialized = js.unserializeToNodes(serialized).stream().filter(n -> n instanceof Sum).map(n -> (Sum)n).collect(Collectors.toList());
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
    List<IntLiteral> unserialized = js.unserializeToNodes(serialized).stream().map(n -> (IntLiteral)n).collect(Collectors.toList());
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
}
