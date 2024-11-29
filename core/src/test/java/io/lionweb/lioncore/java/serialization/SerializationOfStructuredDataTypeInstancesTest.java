package io.lionweb.lioncore.java.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.StructuredDataType;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.StructuredDataTypeInstanceUtils;
import io.lionweb.lioncore.java.model.impl.DynamicStructuredDataTypeInstance;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Specific tests of JsonSerialization for the serialization and deserialization of primitive
 * values.
 */
public class SerializationOfStructuredDataTypeInstancesTest extends SerializationTest {

  @Test
  public void serializeStructuredDataTypeInstances() {
    StructuredDataType point = MyNodeWithStructuredDataType.POINT;
    StructuredDataType address = MyNodeWithStructuredDataType.ADDRESS;

    DynamicStructuredDataTypeInstance sdt1 = new DynamicStructuredDataTypeInstance(point);
    StructuredDataTypeInstanceUtils.setFieldValueByName(sdt1, "x", 10);
    StructuredDataTypeInstanceUtils.setFieldValueByName(sdt1, "y", 20);

    DynamicStructuredDataTypeInstance sdt2 = new DynamicStructuredDataTypeInstance(address);
    StructuredDataTypeInstanceUtils.setFieldValueByName(sdt2, "street", "Via Morghen 29");
    StructuredDataTypeInstanceUtils.setFieldValueByName(sdt2, "city", "Torino");

    MyNodeWithStructuredDataType n1 = new MyNodeWithStructuredDataType("n1");
    n1.setPoint(sdt1);
    n1.setAddress(sdt2);

    JsonObject expected =
        JsonParser.parseString(
                "{\n" +
                        "    \"serializationFormatVersion\": \"2023.1\",\n" +
                        "    \"languages\": [\n" +
                        "        {\n" +
                        "            \"key\": \"mylanguageWithSDT\",\n" +
                        "            \"version\": \"1\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"nodes\": [\n" +
                        "        {\n" +
                        "            \"id\": \"n1\",\n" +
                        "            \"classifier\": {\n" +
                        "                \"language\": \"mylanguageWithSDT\",\n" +
                        "                \"version\": \"1\",\n" +
                        "                \"key\": \"concept-MyNodeWithStructuredDataType\"\n" +
                        "            },\n" +
                        "            \"properties\": [\n" +
                        "                {\n" +
                        "                    \"property\": {\n" +
                        "                        \"language\": \"mylanguageWithSDT\",\n" +
                        "                        \"version\": \"1\",\n" +
                        "                        \"key\": \"my-point\"\n" +
                        "                    },\n" +
                        "                    \"value\": \"{\\\"x\\\":\\\"10\\\",\\\"y\\\":\\\"20\\\"}\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"property\": {\n" +
                        "                        \"language\": \"mylanguageWithSDT\",\n" +
                        "                        \"version\": \"1\",\n" +
                        "                        \"key\": \"my-address\"\n" +
                        "                    },\n" +
                        "                    \"value\": \"{\\\"street\\\":\\\"Via Morghen 29\\\",\\\"city\\\":\\\"Torino\\\"}\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"containments\": [],\n" +
                        "            \"references\": [],\n" +
                        "            \"annotations\": [],\n" +
                        "            \"parent\": null\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
            .getAsJsonObject();
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    jsonSerialization.registerLanguage(MyNodeWithStructuredDataType.LANGUAGE);
    JsonObject serialized = jsonSerialization.serializeNodesToJsonElement(n1).getAsJsonObject();
    SerializedJsonComparisonUtils.assertEquivalentLionWebJson(expected, serialized);
  }

  @Test
  public void deserializeStructuredDataTypeInstance() {
    StructuredDataType point = MyNodeWithStructuredDataType.POINT;
    StructuredDataType address = MyNodeWithStructuredDataType.ADDRESS;

    DynamicStructuredDataTypeInstance sdt1 = new DynamicStructuredDataTypeInstance(point);
    StructuredDataTypeInstanceUtils.setFieldValueByName(sdt1, "x", 10);
    StructuredDataTypeInstanceUtils.setFieldValueByName(sdt1, "y", 20);

    DynamicStructuredDataTypeInstance sdt2 = new DynamicStructuredDataTypeInstance(address);
    StructuredDataTypeInstanceUtils.setFieldValueByName(sdt2, "street", "Via Morghen 29");
    StructuredDataTypeInstanceUtils.setFieldValueByName(sdt2, "city", "Torino");

    MyNodeWithStructuredDataType n1 = new MyNodeWithStructuredDataType("n1");
    n1.setPoint(sdt1);
    n1.setAddress(sdt2);

    JsonObject serialized =
        JsonParser.parseString(
                "{\n" +
                        "    \"serializationFormatVersion\": \"2023.1\",\n" +
                        "    \"languages\": [\n" +
                        "        {\n" +
                        "            \"key\": \"mylanguageWithSDT\",\n" +
                        "            \"version\": \"1\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"nodes\": [\n" +
                        "        {\n" +
                        "            \"id\": \"n1\",\n" +
                        "            \"classifier\": {\n" +
                        "                \"language\": \"mylanguageWithSDT\",\n" +
                        "                \"version\": \"1\",\n" +
                        "                \"key\": \"concept-MyNodeWithStructuredDataType\"\n" +
                        "            },\n" +
                        "            \"properties\": [\n" +
                        "                {\n" +
                        "                    \"property\": {\n" +
                        "                        \"language\": \"mylanguageWithSDT\",\n" +
                        "                        \"version\": \"1\",\n" +
                        "                        \"key\": \"my-point\"\n" +
                        "                    },\n" +
                        "                    \"value\": \"{\\\"x\\\":\\\"10\\\",\\\"y\\\":\\\"20\\\"}\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"property\": {\n" +
                        "                        \"language\": \"mylanguageWithSDT\",\n" +
                        "                        \"version\": \"1\",\n" +
                        "                        \"key\": \"my-address\"\n" +
                        "                    },\n" +
                        "                    \"value\": \"{\\\"street\\\":\\\"Via Morghen 29\\\",\\\"city\\\":\\\"Torino\\\"}\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"containments\": [],\n" +
                        "            \"references\": [],\n" +
                        "            \"annotations\": [],\n" +
                        "            \"parent\": null\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}")
            .getAsJsonObject();
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    jsonSerialization.enableDynamicNodes();
    jsonSerialization.registerLanguage(MyNodeWithStructuredDataType.LANGUAGE);
    jsonSerialization
        .getInstantiator()
        .registerCustomDeserializer(
            MyNodeWithStructuredDataType.CONCEPT.getID(),
            (concept, serializedNode, deserializedNodesByID, propertiesValue) ->
                new MyNodeWithStructuredDataType(serializedNode.getID()));
    List<Node> deserialized = jsonSerialization.deserializeToNodes(serialized);
    assertEquals(Arrays.asList(n1), deserialized);
  }

}
