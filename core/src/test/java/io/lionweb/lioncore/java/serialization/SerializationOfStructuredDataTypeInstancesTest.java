package io.lionweb.lioncore.java.serialization;

import static org.junit.Assert.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.StructuredDataType;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.StructuredDataTypeInstance;
import io.lionweb.lioncore.java.model.StructuredDataTypeInstanceUtils;
import io.lionweb.lioncore.java.model.impl.DynamicStructuredDataTypeInstance;
import io.lionweb.lioncore.java.model.impl.EnumerationValueImpl;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * Specific tests of JsonSerialization for the serialization and deserialization of Structured Data
 * Type Instances.
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
                "{\n"
                    + "    \"serializationFormatVersion\": \"2024.1\",\n"
                    + "    \"languages\": [\n"
                    + "        {\n"
                    + "            \"key\": \"mylanguageWithSDT\",\n"
                    + "            \"version\": \"1\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"nodes\": [\n"
                    + "        {\n"
                    + "            \"id\": \"n1\",\n"
                    + "            \"classifier\": {\n"
                    + "                \"language\": \"mylanguageWithSDT\",\n"
                    + "                \"version\": \"1\",\n"
                    + "                \"key\": \"concept-MyNodeWithStructuredDataType\"\n"
                    + "            },\n"
                    + "            \"properties\": [\n"
                    + "                {\n"
                    + "                    \"property\": {\n"
                    + "                        \"language\": \"mylanguageWithSDT\",\n"
                    + "                        \"version\": \"1\",\n"
                    + "                        \"key\": \"my-point\"\n"
                    + "                    },\n"
                    + "                    \"value\": \"{\\\"x-key\\\":\\\"10\\\",\\\"y-key\\\":\\\"20\\\"}\"\n"
                    + "                },\n"
                    + "                {\n"
                    + "                    \"property\": {\n"
                    + "                        \"language\": \"mylanguageWithSDT\",\n"
                    + "                        \"version\": \"1\",\n"
                    + "                        \"key\": \"my-address\"\n"
                    + "                    },\n"
                    + "                    \"value\": \"{\\\"street-key\\\":\\\"Via Morghen 29\\\",\\\"city-key\\\":\\\"Torino\\\"}\"\n"
                    + "                }\n"
                    + "            ],\n"
                    + "            \"containments\": [],\n"
                    + "            \"references\": [],\n"
                    + "            \"annotations\": [],\n"
                    + "            \"parent\": null\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}")
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
                "{\n"
                    + "    \"serializationFormatVersion\": \"2024.1\",\n"
                    + "    \"languages\": [\n"
                    + "        {\n"
                    + "            \"key\": \"mylanguageWithSDT\",\n"
                    + "            \"version\": \"1\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"nodes\": [\n"
                    + "        {\n"
                    + "            \"id\": \"n1\",\n"
                    + "            \"classifier\": {\n"
                    + "                \"language\": \"mylanguageWithSDT\",\n"
                    + "                \"version\": \"1\",\n"
                    + "                \"key\": \"concept-MyNodeWithStructuredDataType\"\n"
                    + "            },\n"
                    + "            \"properties\": [\n"
                    + "                {\n"
                    + "                    \"property\": {\n"
                    + "                        \"language\": \"mylanguageWithSDT\",\n"
                    + "                        \"version\": \"1\",\n"
                    + "                        \"key\": \"my-point\"\n"
                    + "                    },\n"
                    + "                    \"value\": \"{\\\"x-key\\\":\\\"10\\\",\\\"y-key\\\":\\\"20\\\"}\"\n"
                    + "                },\n"
                    + "                {\n"
                    + "                    \"property\": {\n"
                    + "                        \"language\": \"mylanguageWithSDT\",\n"
                    + "                        \"version\": \"1\",\n"
                    + "                        \"key\": \"my-address\"\n"
                    + "                    },\n"
                    + "                    \"value\": \"{\\\"street-key\\\":\\\"Via Morghen 29\\\",\\\"city-key\\\":\\\"Torino\\\"}\"\n"
                    + "                }\n"
                    + "            ],\n"
                    + "            \"containments\": [],\n"
                    + "            \"references\": [],\n"
                    + "            \"annotations\": [],\n"
                    + "            \"parent\": null\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}")
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

  @Test
  public void serializeAndDeserializationMultiLevelStructuredDataTypeInstances() {
    DynamicStructuredDataTypeInstance value =
        new DynamicStructuredDataTypeInstance(MyNodeWithAmount.DECIMAL);
    StructuredDataTypeInstanceUtils.setFieldValueByName(value, "int", 2);
    StructuredDataTypeInstanceUtils.setFieldValueByName(value, "frac", 3);

    DynamicStructuredDataTypeInstance amount =
        new DynamicStructuredDataTypeInstance(MyNodeWithAmount.AMOUNT);
    StructuredDataTypeInstanceUtils.setFieldValueByName(amount, "value", value);
    StructuredDataTypeInstanceUtils.setFieldValueByName(
        amount,
        "currency",
        new EnumerationValueImpl(MyNodeWithAmount.CURRENCY.getLiterals().get(0)));
    StructuredDataTypeInstanceUtils.setFieldValueByName(amount, "digital", true);

    MyNodeWithAmount n1 = new MyNodeWithAmount("n1");
    n1.setAmount(amount);
    assertEquals(amount, n1.getAmount());

    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    jsonSerialization.registerLanguage(MyNodeWithAmount.LANGUAGE);
    jsonSerialization.enableDynamicNodes();
    String currencySerialized =
        jsonSerialization
            .getPrimitiveValuesSerialization()
            .serialize(
                MyNodeWithAmount.CURRENCY.getID(),
                new EnumerationValueImpl(MyNodeWithAmount.CURRENCY.getLiterals().get(0)));
    assertEquals("euro", currencySerialized);
    String amountSerialized =
        jsonSerialization
            .getPrimitiveValuesSerialization()
            .serialize(MyNodeWithAmount.AMOUNT.getID(), amount);
    assertEquals(
        "{\"value-key\":{\"int-key\":\"2\",\"frac-key\":\"3\"},\"currency-key\":\"euro\",\"digital-key\":\"true\"}",
        amountSerialized);
    JsonObject serialized = jsonSerialization.serializeNodesToJsonElement(n1).getAsJsonObject();
    Node n1Deserialized = jsonSerialization.deserializeToNodes(serialized).get(0);
    assertEquals(
        StructuredDataTypeInstanceUtils.getFieldValueByName(n1.getAmount(), "currency"),
        StructuredDataTypeInstanceUtils.getFieldValueByName(
            (StructuredDataTypeInstance)
                ClassifierInstanceUtils.getPropertyValueByName(n1Deserialized, "amount"),
            "currency"));
    assertEquals(
        n1.getAmount(), ClassifierInstanceUtils.getPropertyValueByName(n1Deserialized, "amount"));
    assertEquals(n1, n1Deserialized);
  }
}
