package io.lionweb.serialization;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.language.*;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedReferenceValue;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for issue #411: references to LionCore-builtins elements (LionWeb 2024.1) must
 * be serialized without a target reference id, relying on the (qualified) resolveInfo instead. See
 * specification section 3.4 "Serialization of predefined keys".
 */
public class SerializationOfBuiltinReferencesTest extends SerializationTest {

  private static final String STRING_RESOLVE_INFO = "LionWeb.LionCore_builtins.String";

  private SerializedReferenceValue.Entry singleTypeEntry(
      SerializationChunk chunk, String instanceId, String referenceKey) {
    SerializedClassifierInstance instance = chunk.getClassifierInstancesByID().get(instanceId);
    assertNotNull(instance, "No serialized instance with id " + instanceId);
    SerializedReferenceValue typeRef =
        instance.getReferences().stream()
            .filter(r -> referenceKey.equals(r.getMetaPointer().getKey()))
            .findFirst()
            .orElseThrow(
                () ->
                    new AssertionError(
                        "No reference with key " + referenceKey + " on " + instanceId));
    List<SerializedReferenceValue.Entry> entries = typeRef.getValue();
    assertEquals(1, entries.size());
    return entries.get(0);
  }

  /** A Property whose type is the builtin String. */
  @Test
  public void propertyTypeReferenceToBuiltinIsDangling() {
    Language language =
        new Language().setID("l-id").setKey("l-key").setName("MyLanguage").setVersion("1");
    Concept concept =
        new Concept().setID("c-id").setKey("c-key").setName("MyConcept").setParent(language);
    Property property =
        Property.createOptional("name", LionCoreBuiltins.getString()).setID("p-id").setKey("p-key");
    concept.addFeature(property);
    language.addElement(concept);

    JsonSerialization serialization = SerializationProvider.getStandardJsonSerialization();
    SerializationChunk chunk = serialization.serializeTreeToSerializationChunk(language);

    SerializedReferenceValue.Entry entry = singleTypeEntry(chunk, "p-id", "Property-type");
    assertNull(entry.getReference(), "Reference id to builtin should be null");
    assertEquals(STRING_RESOLVE_INFO, entry.getResolveInfo());
  }

  /** A StructuredDataType Field whose type is the builtin String (second variant in issue #411). */
  @Test
  public void fieldTypeReferenceToBuiltinIsDangling() {
    Language language =
        new Language().setID("l-id").setKey("l-key").setName("MyLanguage").setVersion("1");
    Field field = new Field("street", LionCoreBuiltins.getString()).setID("f-id").setKey("f-key");
    StructuredDataType sdt =
        new StructuredDataType()
            .setID("sdt-id")
            .setKey("sdt-key")
            .setName("address")
            .setParent(language)
            .addField(field);
    language.addElement(sdt);

    JsonSerialization serialization = SerializationProvider.getStandardJsonSerialization();
    SerializationChunk chunk = serialization.serializeTreeToSerializationChunk(language);

    SerializedReferenceValue.Entry entry = singleTypeEntry(chunk, "f-id", "Field-type");
    assertNull(entry.getReference(), "Reference id to builtin should be null");
    assertEquals(STRING_RESOLVE_INFO, entry.getResolveInfo());
  }

  /**
   * For LionWeb 2023.1 the reference id is kept, as there is no qualified resolveInfo to resolve.
   */
  @Test
  public void propertyTypeReferenceToBuiltinKeepsIdIn2023() {
    Language language =
        new Language(io.lionweb.LionWebVersion.v2023_1)
            .setID("l-id")
            .setKey("l-key")
            .setName("MyLanguage")
            .setVersion("1");
    Concept concept =
        new Concept(io.lionweb.LionWebVersion.v2023_1)
            .setID("c-id")
            .setKey("c-key")
            .setName("MyConcept")
            .setParent(language);
    Property property =
        Property.createOptional(
                io.lionweb.LionWebVersion.v2023_1,
                "name",
                LionCoreBuiltins.getString(io.lionweb.LionWebVersion.v2023_1))
            .setID("p-id")
            .setKey("p-key");
    concept.addFeature(property);
    language.addElement(concept);

    JsonSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(io.lionweb.LionWebVersion.v2023_1);
    SerializationChunk chunk = serialization.serializeTreeToSerializationChunk(language);

    SerializedReferenceValue.Entry entry = singleTypeEntry(chunk, "p-id", "Property-type");
    assertNotNull(entry.getReference(), "Reference id should be kept for 2023.1");
  }
}
