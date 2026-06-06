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
 * correct output and that round-trips are lossless.
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

  // ---- Round-trip tests ----

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
  void directDeserializerRoundTrip() throws IOException {
    Sum sum = new Sum(new IntLiteral(5), new IntLiteral(6));
    ProtoBufSerialization s = standardSerialization();

    byte[] bytes = s.serializeNodesToByteArray(sum);
    SerializationChunk viaDirect = DirectProtoBufDeserializer.deserialize(bytes, true);

    assertNotNull(viaDirect);
    assertFalse(viaDirect.getClassifierInstances().isEmpty());
  }

  @Test
  void largeLanguageRoundTrip() throws IOException {
    InputStream is = getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    assertNotNull(is, "LargeLanguage.json must be on classpath");
    String json;
    try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
      json = r.lines().collect(Collectors.joining("\n"));
    }

    SerializationChunk originalChunk =
        new LowLevelJsonSerialization().deserializeSerializationBlock(json);

    byte[] directBytes = DirectProtoBufSerializer.serialize(originalChunk, true);
    SerializationChunk directChunk = DirectProtoBufDeserializer.deserialize(directBytes, true);

    assertEquals(
        originalChunk,
        directChunk,
        "Large language: direct deserialized chunk must equal original");
  }

  @Test
  void directSerializerRoundTripForLargeLanguage() throws IOException {
    InputStream is = getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    assertNotNull(is);
    String json;
    try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
      json = r.lines().collect(Collectors.joining("\n"));
    }

    SerializationChunk chunk = new LowLevelJsonSerialization().deserializeSerializationBlock(json);
    byte[] bytes = DirectProtoBufSerializer.serialize(chunk, true);
    SerializationChunk restored = DirectProtoBufDeserializer.deserialize(bytes, true);

    assertEquals(chunk, restored, "Large language: serialize → deserialize must be lossless");
  }

  private SerializationChunk loadLargeLanguage() throws IOException {
    InputStream is = getClass().getResourceAsStream("/serialization/LargeLanguage.json");
    assertNotNull(is, "LargeLanguage.json must be on classpath");
    String json;
    try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
      json = r.lines().collect(Collectors.joining("\n"));
    }
    return new LowLevelJsonSerialization().deserializeSerializationBlock(json);
  }

  @Test
  void frequencySortingProducesSmallerOrEqualOutput() throws IOException {
    SerializationChunk chunk = loadLargeLanguage();

    byte[] sorted = DirectProtoBufSerializer.serialize(chunk, true);
    byte[] unsorted = DirectProtoBufSerializer.serializeUnsorted(chunk, true);

    int savings = unsorted.length - sorted.length;
    double pct = 100.0 * savings / unsorted.length;
    System.out.printf(
        "[size] unsorted=%d B  sorted=%d B  savings=%d B (%.2f%%)%n",
        unsorted.length, sorted.length, savings, pct);

    assertTrue(
        sorted.length <= unsorted.length,
        "Frequency-sorted output ("
            + sorted.length
            + " B) should be <= unsorted ("
            + unsorted.length
            + " B)");
  }

  @Test
  void frequencySortingSerializationTimeOverhead() throws IOException {
    SerializationChunk chunk = loadLargeLanguage();

    int warmupRounds = 20;
    int measureRounds = 100;

    // Warmup
    for (int i = 0; i < warmupRounds; i++) {
      DirectProtoBufSerializer.serialize(chunk, true);
      DirectProtoBufSerializer.serializeUnsorted(chunk, true);
    }

    // Measure unsorted
    long t0 = System.nanoTime();
    for (int i = 0; i < measureRounds; i++) {
      DirectProtoBufSerializer.serializeUnsorted(chunk, true);
    }
    long unsortedNs = (System.nanoTime() - t0) / measureRounds;

    // Measure sorted
    t0 = System.nanoTime();
    for (int i = 0; i < measureRounds; i++) {
      DirectProtoBufSerializer.serialize(chunk, true);
    }
    long sortedNs = (System.nanoTime() - t0) / measureRounds;

    double overhead = 100.0 * (sortedNs - unsortedNs) / unsortedNs;
    System.out.printf(
        "[perf] unsorted=%.2f ms  sorted=%.2f ms  overhead=%.1f%%%n",
        unsortedNs / 1e6, sortedNs / 1e6, overhead);

    // Sorted should not be more than 3× slower; the extra pass is lightweight.
    assertTrue(
        sortedNs < unsortedNs * 3,
        String.format(
            "Frequency sort overhead too large: unsorted=%.2f ms, sorted=%.2f ms",
            unsortedNs / 1e6, sortedNs / 1e6));
  }
}
