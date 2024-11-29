package io.lionweb.lioncore.java.serialization;

import static org.junit.Assert.assertEquals;

import io.lionweb.lioncore.java.language.Language;
import org.junit.Test;

public class SerializationOfStructuredDataTypesTest extends SerializationTest {

  @Test
  public void serializeAndDeserializeLanguageWithSDTs() {
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    String serialized =
        jsonSerialization.serializeTreesToJsonString(MyNodeWithStructuredDataType.LANGUAGE);
    Language deserialized = (Language) jsonSerialization.deserializeToNodes(serialized).get(0);

    Language expected = MyNodeWithStructuredDataType.LANGUAGE;
    assertEquals(expected.getName(), deserialized.getName());
    assertEquals(expected.getID(), deserialized.getID());
    assertEquals(expected.getKey(), deserialized.getKey());
    assertEquals(expected.getElements().size(), deserialized.getElements().size());
    assertEquals(2, deserialized.getStructuredDataTypes().size());
  }
}
