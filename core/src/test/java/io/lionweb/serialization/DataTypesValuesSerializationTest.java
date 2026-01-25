package io.lionweb.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.language.Enumeration;
import io.lionweb.model.StructuredDataTypeInstance;
import io.lionweb.model.impl.DynamicStructuredDataTypeInstance;
import io.lionweb.model.impl.EnumerationValue;
import io.lionweb.model.impl.EnumerationValueImpl;
import io.lionweb.model.impl.M3Node;
import org.junit.Before;
import org.junit.jupiter.api.Test;

public class DataTypesValuesSerializationTest {

  private DataTypesValuesSerialization serialization;
  private Language testLanguage;
  private Enumeration testEnumeration;
  private StructuredDataType testStructuredDataType;
  private PrimitiveType stringType;
  private PrimitiveType integerType;
  private PrimitiveType booleanType;

  @Before
  public void setUp() {
    serialization = new DataTypesValuesSerialization();

    // Create test language with enumeration and structured data type
    testLanguage = new Language(LionWebVersion.v2024_1, "testLang");
    testLanguage.setID("test-lang-id");
    testLanguage.setKey("test-lang-key");
    testLanguage.setVersion("1");

    // Create test enumeration
    testEnumeration = new Enumeration(testLanguage, "TestEnum", "test-enum-id");
    testEnumeration.setKey("test-enum-key");

    EnumerationLiteral literal1 =
        new EnumerationLiteral(testEnumeration, "OPTION_ONE", "literal1-id");
    literal1.setKey("option_one");

    EnumerationLiteral literal2 =
        new EnumerationLiteral(testEnumeration, "OPTION_TWO", "literal2-id");
    literal2.setKey("option_two");

    testEnumeration.addLiteral(literal1);
    testEnumeration.addLiteral(literal2);

    // Create test structured data type
    testStructuredDataType = new StructuredDataType(testLanguage, "TestSDT", "test-sdt-id");
    testStructuredDataType.setKey("test-sdt-key");

    stringType = LionCoreBuiltins.getString(LionWebVersion.v2024_1);
    integerType = LionCoreBuiltins.getInteger(LionWebVersion.v2024_1);
    booleanType = LionCoreBuiltins.getBoolean(LionWebVersion.v2024_1);

    Field stringField = new Field(testStructuredDataType, "stringField");
    stringField.setID("string-field-id");
    stringField.setKey("stringField");
    stringField.setType(stringType);

    Field intField = new Field(testStructuredDataType, "intField");
    intField.setID("int-field-id");
    intField.setKey("intField");
    intField.setType(integerType);

    testStructuredDataType.addField(stringField);
    testStructuredDataType.addField(intField);

    testLanguage.addElement(testEnumeration);
    testLanguage.addElement(testStructuredDataType);
  }

  @Test
  public void testRegisterLanguage() {
    serialization.registerLanguage(testLanguage);

    // Verify enumeration is registered by trying to deserialize
    serialization.enableDynamicNodes();
    Object result = serialization.deserialize(testEnumeration, "option_one", true);

    assertNotNull(result);
    assertTrue(result instanceof EnumerationValue);
    assertEquals("option_one", ((EnumerationValue) result).getEnumerationLiteral().getKey());
  }

  @Test
  public void testEnableDynamicNodes() {
    serialization.registerLanguage(testLanguage);

    // Without dynamic nodes enabled, should throw exception
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          serialization.deserialize(testEnumeration, "option_one", true);
        });

    // With dynamic nodes enabled, should work
    serialization.enableDynamicNodes();
    Object result = serialization.deserialize(testEnumeration, "option_one", true);
    assertNotNull(result);
  }

  @Test
  public void testRegisterCustomSerializer() {
    DataTypesValuesSerialization.DataTypeSerializer<String> customSerializer =
        value -> "CUSTOM:" + value;

    serialization.registerSerializer("custom-type-id", customSerializer);

    String result = serialization.serialize("custom-type-id", "test");
    assertEquals("CUSTOM:test", result);
  }

  @Test
  public void testRegisterCustomDeserializer() {
    DataTypesValuesSerialization.DataTypeDeserializer<String> customDeserializer =
        serializedValue -> serializedValue.replace("CUSTOM:", "");

    serialization.registerDeserializer("custom-type-id", customDeserializer);

    DataType<? extends M3Node> customType =
        new PrimitiveType(LionWebVersion.v2024_1, "CustomType") {
          @Override
          public String getID() {
            return "custom-type-id";
          }
        };

    Object result = serialization.deserialize(customType, "CUSTOM:test", true);
    assertEquals("test", result);
  }

  @Test
  public void testRegisterBuiltinSerializersAndDeserializers() {
    serialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers(LionWebVersion.v2024_1);

    // Test boolean serialization/deserialization
    String serializedBoolean = serialization.serialize(booleanType.getID(), true);
    assertEquals("true", serializedBoolean);

    Object deserializedBoolean = serialization.deserialize(booleanType, "true", true);
    assertEquals(true, deserializedBoolean);

    // Test string serialization/deserialization
    String serializedString = serialization.serialize(stringType.getID(), "hello");
    assertEquals("hello", serializedString);

    Object deserializedString = serialization.deserialize(stringType, "hello", true);
    assertEquals("hello", deserializedString);

    // Test integer serialization/deserialization
    String serializedInteger = serialization.serialize(integerType.getID(), 42);
    assertEquals("42", serializedInteger);

    Object deserializedInteger = serialization.deserialize(integerType, "42", true);
    assertEquals(42, deserializedInteger);
  }

  @Test
  public void testJsonSerializationDeserialization() {
    serialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers(LionWebVersion.v2023_1);

    PrimitiveType jsonType = LionCoreBuiltins.getJSON(LionWebVersion.v2023_1);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("name", "test");
    jsonObject.addProperty("value", 123);

    String serialized = serialization.serialize(jsonType.getID(), jsonObject);
    assertNotNull(serialized);

    Object deserialized = serialization.deserialize(jsonType, serialized, true);
    assertTrue(deserialized instanceof JsonElement);
    JsonObject deserializedObject = ((JsonElement) deserialized).getAsJsonObject();
    assertEquals("test", deserializedObject.get("name").getAsString());
    assertEquals(123, deserializedObject.get("value").getAsInt());
  }

  @Test
  public void testBooleanDeserializationWithOptionalValue() {
    serialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers(LionWebVersion.v2024_1);

    // Test with required = false and null value
    Object result = serialization.deserialize(booleanType, null, false);
    assertNull(result);

    // Test with required = true should still work with valid value
    Object result2 = serialization.deserialize(booleanType, "false", true);
    assertEquals(false, result2);
  }

  @Test
  public void testEnumerationSerialization() {
    serialization.registerLanguage(testLanguage);
    serialization.enableDynamicNodes();

    EnumerationValue enumValue = new EnumerationValueImpl(testEnumeration.getLiterals().get(0));
    String serialized = serialization.serialize(testEnumeration.getID(), enumValue);
    assertEquals("option_one", serialized);

    // Test null enumeration value
    String nullSerialized = serialization.serialize(testEnumeration.getID(), null);
    assertNull(nullSerialized);
  }

  @Test
  public void testEnumerationDeserialization() {
    serialization.registerLanguage(testLanguage);
    serialization.enableDynamicNodes();

    Object deserialized = serialization.deserialize(testEnumeration, "option_two", true);
    assertNotNull(deserialized);
    assertTrue(deserialized instanceof EnumerationValue);
    assertEquals("option_two", ((EnumerationValue) deserialized).getEnumerationLiteral().getKey());

    // Test null deserialization
    Object nullDeserialized = serialization.deserialize(testEnumeration, null, false);
    assertNull(nullDeserialized);
  }

  @Test
  public void testEnumerationDeserializationWithNullValue() {
    serialization.registerLanguage(testLanguage);
    serialization.enableDynamicNodes();

    Object deserializedWhenNotRequired = serialization.deserialize(testEnumeration, null, false);
    assertNull(deserializedWhenNotRequired);

    Object deserializedWhenRequired = serialization.deserialize(testEnumeration, null, true);
    assertNull(deserializedWhenRequired);
  }

  @Test
  public void testInvalidEnumerationLiteral() {
    serialization.registerLanguage(testLanguage);
    serialization.enableDynamicNodes();

    assertThrows(
        RuntimeException.class,
        () -> {
          serialization.deserialize(testEnumeration, "invalid_literal", true);
        });
  }

  @Test
  public void testStructuredDataTypeSerialization() {
    serialization.registerLanguage(testLanguage);
    serialization.enableDynamicNodes();
    serialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers(LionWebVersion.v2024_1);

    DynamicStructuredDataTypeInstance instance =
        new DynamicStructuredDataTypeInstance(testStructuredDataType);
    instance.setFieldValue(testStructuredDataType.getFields().get(0), "test string");
    instance.setFieldValue(testStructuredDataType.getFields().get(1), 42);

    String serialized = serialization.serialize(testStructuredDataType.getID(), instance);
    assertNotNull(serialized);
    assertTrue(serialized.contains("test string"));
    assertTrue(serialized.contains("42"));

    // Test null structured data type
    String nullSerialized = serialization.serialize(testStructuredDataType.getID(), null);
    assertNull(nullSerialized);
  }

  @Test
  public void testStructuredDataTypeDeserialization() {
    serialization.registerLanguage(testLanguage);
    serialization.enableDynamicNodes();
    serialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers(LionWebVersion.v2024_1);

    String jsonString = "{\"stringField\":\"test string\",\"intField\":\"42\"}";

    Object deserialized = serialization.deserialize(testStructuredDataType, jsonString, true);
    assertNotNull(deserialized);
    assertTrue(deserialized instanceof StructuredDataTypeInstance);

    StructuredDataTypeInstance sdtInstance = (StructuredDataTypeInstance) deserialized;
    assertEquals(
        "test string", sdtInstance.getFieldValue(testStructuredDataType.getFields().get(0)));
    assertEquals(42, sdtInstance.getFieldValue(testStructuredDataType.getFields().get(1)));

    // Test null deserialization
    Object nullDeserialized = serialization.deserialize(testStructuredDataType, null, false);
    assertNull(nullDeserialized);
  }

  @Test
  public void testStructuredDataTypeWithNullFields() {
    serialization.registerLanguage(testLanguage);
    serialization.enableDynamicNodes();
    serialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers(LionWebVersion.v2024_1);

    DynamicStructuredDataTypeInstance instance =
        new DynamicStructuredDataTypeInstance(testStructuredDataType);
    instance.setFieldValue(testStructuredDataType.getFields().get(0), null);
    instance.setFieldValue(testStructuredDataType.getFields().get(1), 42);

    String serialized = serialization.serialize(testStructuredDataType.getID(), instance);
    assertNotNull(serialized);
    assertTrue(serialized.contains("null"));
    assertTrue(serialized.contains("42"));
  }

  enum TestEnum2 {
    OPTION_ONE,
    OPTION_TWO
  }

  @Test
  public void testSerializerForEnum() {

    DataTypesValuesSerialization.DataTypeSerializer<TestEnum2> serializer =
        DataTypesValuesSerialization.serializerFor(TestEnum2.class, testEnumeration);

    String result = serializer.serialize(TestEnum2.OPTION_ONE);
    assertEquals("option_one", result);
  }

  enum TestEnum3 {
    OPTION_ONE,
    OPTION_TWO
  }

  @Test
  public void testDeserializerForEnum() {

    DataTypesValuesSerialization.DataTypeDeserializer<TestEnum3> deserializer =
        DataTypesValuesSerialization.deserializerFor(TestEnum3.class, testEnumeration);

    TestEnum3 result = deserializer.deserialize("option_one");
    assertEquals(TestEnum3.OPTION_ONE, result);
  }

  enum TestEnum4 {
    MISSING_VALUE
  }

  @Test
  public void testSerializerForEnumWithMissingLiteral() {

    DataTypesValuesSerialization.DataTypeSerializer<TestEnum4> serializer =
        DataTypesValuesSerialization.serializerFor(TestEnum4.class, testEnumeration);

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          serializer.serialize(TestEnum4.MISSING_VALUE);
        });
  }

  enum TestEnum5 {
    OPTION_ONE,
    OPTION_TWO
  }

  @Test
  public void testDeserializerForEnumWithMissingKey() {

    DataTypesValuesSerialization.DataTypeDeserializer<TestEnum5> deserializer =
        DataTypesValuesSerialization.deserializerFor(TestEnum5.class, testEnumeration);

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          deserializer.deserialize("missing_key");
        });
  }

  @Test
  public void testRegisterSerializerWithNullArguments() {
    assertThrows(
        NullPointerException.class,
        () -> {
          serialization.registerSerializer((PrimitiveType) null, value -> "test");
        });

    assertThrows(
        NullPointerException.class,
        () -> {
          serialization.registerSerializer("test-id", null);
        });
  }

  @Test
  public void testRegisterDeserializerWithNullArguments() {
    assertThrows(
        NullPointerException.class,
        () -> {
          serialization.registerDeserializer((PrimitiveType) null, value -> "test");
        });

    assertThrows(
        NullPointerException.class,
        () -> {
          serialization.registerDeserializer("test-id", null);
        });
  }

  @Test
  public void testRegisterBuiltinsWithNullVersion() {
    assertThrows(
        NullPointerException.class,
        () -> {
          serialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers(null);
        });
  }

  @Test
  public void testDeserializeWithNullDataType() {
    assertThrows(
        NullPointerException.class,
        () -> {
          serialization.deserialize(null, "test", true);
        });
  }

  @Test
  public void testSerializeWithNullPrimitiveTypeID() {
    assertThrows(
        NullPointerException.class,
        () -> {
          serialization.serialize(null, "test");
        });
  }

  @Test
  public void testUnknownDataTypeDeserialization() {
    DataType<? extends M3Node> unknownType =
        new PrimitiveType(LionWebVersion.v2024_1, "UnknownType") {
          @Override
          public String getID() {
            return "unknown-type-id";
          }
        };

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          serialization.deserialize(unknownType, "test", true);
        });
  }

  @Test
  public void testUnknownPrimitiveTypeSerialization() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          serialization.serialize("unknown-type-id", "test");
        });
  }

  @Test
  public void testEnumSerializationWithNullKey() {
    EnumerationLiteral literalWithNullKey =
        new EnumerationLiteral(testEnumeration, "NULL_KEY", "null-key-literal-id");
    literalWithNullKey.setKey(null);

    EnumerationValue enumValue = new EnumerationValueImpl(literalWithNullKey);

    serialization.registerLanguage(testLanguage);
    serialization.enableDynamicNodes();

    assertThrows(
        IllegalStateException.class,
        () -> {
          serialization.serialize(testEnumeration.getID(), enumValue);
        });
  }

  @Test
  public void testWrongTypeForEnumSerialization() {
    serialization.registerLanguage(testLanguage);
    serialization.enableDynamicNodes();

    assertThrows(
        IllegalStateException.class,
        () -> {
          serialization.serialize(testEnumeration.getID(), "not an enum");
        });
  }

  @Test
  public void testWrongTypeForStructuredDataTypeSerialization() {
    serialization.registerLanguage(testLanguage);
    serialization.enableDynamicNodes();

    assertThrows(
        IllegalStateException.class,
        () -> {
          serialization.serialize(testStructuredDataType.getID(), "not a structured data type");
        });
  }
}
