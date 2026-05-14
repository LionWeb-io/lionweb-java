package io.lionweb.serialization;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.model.Node;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.simplemath.IntLiteral;
import io.lionweb.serialization.simplemath.SimpleMathLanguage;
import io.lionweb.serialization.simplemath.Sum;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Validates that {@link DirectProtoBufSerializer} and {@link DirectProtoBufDeserializer} produce
 * output that is byte-identical to the standard protobuf path and that round-trips are lossless.
 */
class DirectProtoBufTest {

  // ---- Helpers ----

  private ProtoBufSerialization standardSerialization() {
    return SerializationProvider.getStandardProtoBufSerialization();
  }

  private void prepareForSimpleMath(ProtoBufSerialization s) {
    s.getClassifierResolver().registerLanguage(SimpleMathLanguage.INSTANCE);
    s.getInstantiator()
        .registerCustomDeserializer(
            SimpleMathLanguage.INT_LITERAL.getID(),
            (concept, serializedNode, byId, props) ->
                new IntLiteral(
                    (Integer) props.get(concept.getPropertyByName("value")),
                    serializedNode.getID()));
    s.getInstantiator()
        .registerCustomDeserializer(
            SimpleMathLanguage.SUM.getID(),
            (concept, serializedNode, byId, props) -> {
              String leftId =
                  serializedNode.getContainments().stream()
                      .filter(c -> c.getMetaPointer().getKey().equals("SimpleMath_Sum_left"))
                      .findFirst()
                      .get()
                      .getChildrenIds()
                      .get(0);
              String rightId =
                  serializedNode.getContainments().stream()
                      .filter(c -> c.getMetaPointer().getKey().equals("SimpleMath_Sum_right"))
                      .findFirst()
                      .get()
                      .getChildrenIds()
                      .get(0);
              return new Sum(
                  (IntLiteral) byId.get(leftId),
                  (IntLiteral) byId.get(rightId),
                  serializedNode.getID());
            });
  }

  // ---- Byte-identity tests ----

  @Test
  void directSerializerProducesSameBytesAsLegacy() throws IOException {
    Sum sum = new Sum(new IntLiteral(3), new IntLiteral(7));
    ProtoBufSerialization s = standardSerialization();
    // Build the same SerializationChunk and compare both serializers against it
    SerializationChunk chunk = s.serializeTreesToSerializationChunk(List.of(sum));
    byte[] legacy = s.serialize(chunk).toByteArray();
    byte[] direct = DirectProtoBufSerializer.serialize(chunk, true);
    assertArrayEquals(
        legacy, direct, "Direct serializer must produce identical bytes to legacy path");
  }

  @Test
  void directSerializerRoundTripSimpleMath() throws IOException {
    Sum original = new Sum(new IntLiteral(1), new IntLiteral(2));
    ProtoBufSerialization s = standardSerialization();
    prepareForSimpleMath(s);

    byte[] bytes = s.serializeTreesToByteArray(original);
    List<Node> restored = s.deserializeToNodes(bytes);

    assertEquals(1, restored.stream().filter(n -> n instanceof Sum).count());
    Sum restoredSum = (Sum) restored.stream().filter(n -> n instanceof Sum).findFirst().get();
    assertEquals(original, restoredSum);
  }

  @Test
  void directDeserializerProducesSameChunkAsLegacy() throws IOException {
    Sum sum = new Sum(new IntLiteral(5), new IntLiteral(6));
    ProtoBufSerialization s = standardSerialization();

    byte[] bytes = s.serializeNodesToByteArray(sum);

    SerializationChunk viaDirect = DirectProtoBufDeserializer.deserialize(bytes, true);
    SerializationChunk viaLegacy = s.deserializeToChunkViaPbChunk(bytes);

    assertEquals(viaLegacy, viaDirect);
  }

  @Test
  void largeLanguageRoundTrip() throws IOException {
    InputStream is = getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    assertNotNull(is, "LargeLanguage.json must be on classpath");
    String json;
    try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
      json = r.lines().collect(Collectors.joining("\n"));
    }

    // Use low-level deserialization to get a SerializationChunk (no language registration needed)
    SerializationChunk originalChunk =
        new LowLevelJsonSerialization().deserializeSerializationBlock(json);

    ProtoBufSerialization s = SerializationProvider.getStandardProtoBufSerialization();

    // Direct serialize → direct deserialize
    byte[] directBytes = DirectProtoBufSerializer.serialize(originalChunk, true);
    SerializationChunk directChunk = DirectProtoBufDeserializer.deserialize(directBytes, true);

    // Legacy serialize → legacy deserialize
    SerializationChunk legacyChunk = s.deserializeToChunkViaPbChunk(directBytes);

    assertEquals(
        originalChunk,
        directChunk,
        "Large language: direct deserialized chunk must equal original");
    assertEquals(
        legacyChunk,
        directChunk,
        "Large language: direct deserialized chunk must equal legacy chunk");
  }

  @Test
  void directSerializerMatchesLegacyForLargeLanguage() throws IOException {
    InputStream is = getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    assertNotNull(is);
    String json;
    try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
      json = r.lines().collect(Collectors.joining("\n"));
    }

    // Use low-level deserialization to get a SerializationChunk (no language registration needed)
    SerializationChunk chunk = new LowLevelJsonSerialization().deserializeSerializationBlock(json);

    ProtoBufSerialization s = SerializationProvider.getStandardProtoBufSerialization();
    byte[] legacy = s.serialize(chunk).toByteArray();
    byte[] direct = DirectProtoBufSerializer.serialize(chunk, true);

    assertArrayEquals(
        legacy,
        direct,
        "Large language: direct serializer must produce identical bytes to legacy path");
  }
}
