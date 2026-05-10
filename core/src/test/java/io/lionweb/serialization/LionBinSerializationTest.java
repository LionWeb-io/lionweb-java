package io.lionweb.serialization;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.model.Node;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.refsmm.ContainerNode;
import io.lionweb.serialization.refsmm.RefNode;
import io.lionweb.serialization.refsmm.RefsLanguage;
import io.lionweb.serialization.simplemath.IntLiteral;
import io.lionweb.serialization.simplemath.SimpleMathLanguage;
import io.lionweb.serialization.simplemath.Sum;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class LionBinSerializationTest extends SerializationTest {

  // =========================================================================
  // Helpers: mirror the setup used in ProtobufSerializationTest
  // =========================================================================

  private LionBinSerialization prepareForSimpleMath() {
    LionBinSerialization s = SerializationProvider.getStandardLionBinSerialization();
    s.getClassifierResolver().registerLanguage(SimpleMathLanguage.INSTANCE);
    s.getInstantiator()
        .registerCustomDeserializer(
            SimpleMathLanguage.INT_LITERAL.getID(),
            (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
                new IntLiteral(
                    (Integer) propertiesValues.get(concept.getPropertyByName("value")),
                    serializedNode.getID()));
    s.getInstantiator()
        .registerCustomDeserializer(
            SimpleMathLanguage.SUM.getID(),
            (concept, serializedNode, deserializedNodesByID, propertiesValues) -> {
              String leftID =
                  serializedNode.getContainments().stream()
                      .filter(c -> c.getMetaPointer().getKey().equals("SimpleMath_Sum_left"))
                      .findFirst()
                      .get()
                      .getChildrenIds()
                      .get(0);
              String rightID =
                  serializedNode.getContainments().stream()
                      .filter(c -> c.getMetaPointer().getKey().equals("SimpleMath_Sum_right"))
                      .findFirst()
                      .get()
                      .getChildrenIds()
                      .get(0);
              IntLiteral left = (IntLiteral) deserializedNodesByID.get(leftID);
              IntLiteral right = (IntLiteral) deserializedNodesByID.get(rightID);
              return new Sum(left, right, serializedNode.getID());
            });
    return s;
  }

  private LionBinSerialization prepareForRefs() {
    LionBinSerialization s = SerializationProvider.getStandardLionBinSerialization();
    s.getClassifierResolver().registerLanguage(RefsLanguage.INSTANCE);
    s.getInstantiator()
        .registerCustomDeserializer(
            RefsLanguage.CONTAINER_NODE.getID(),
            (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
                new ContainerNode(
                    (ContainerNode) propertiesValues.get(concept.getContainmentByName("contained")),
                    serializedNode.getID()));
    s.getInstantiator()
        .registerCustomDeserializer(
            RefsLanguage.REF_NODE.getID(),
            (concept, serializedNode, deserializedNodesByID, propertiesValues) ->
                new RefNode(serializedNode.getID()));
    return s;
  }

  // =========================================================================
  // Round-trip: single tree
  // =========================================================================

  @Test
  public void singleTreeRoundTrip() throws IOException {
    Sum original = new Sum(new IntLiteral(3), new IntLiteral(7));
    LionBinSerialization s = prepareForSimpleMath();
    byte[] bytes = s.serializeTreesToByteArray(original);
    List<Sum> result =
        s.deserializeToNodes(bytes).stream()
            .filter(n -> n instanceof Sum)
            .map(n -> (Sum) n)
            .collect(Collectors.toList());
    assertEquals(1, result.size());
    assertEquals(original, result.get(0));
  }

  // =========================================================================
  // Round-trip: multiple roots of same classifier → type table reuse
  // =========================================================================

  @Test
  public void multipleRootsRoundTrip() throws IOException {
    Sum sum1 = new Sum(new IntLiteral(1), new IntLiteral(2));
    Sum sum2 = new Sum(new IntLiteral(3), new IntLiteral(4));
    LionBinSerialization s = prepareForSimpleMath();
    byte[] bytes = s.serializeTreesToByteArray(sum1, sum2);
    List<Sum> result =
        s.deserializeToNodes(bytes).stream()
            .filter(n -> n instanceof Sum)
            .map(n -> (Sum) n)
            .collect(Collectors.toList());
    assertEquals(Arrays.asList(sum1, sum2), result);
  }

  // =========================================================================
  // Node ordering preserved
  // =========================================================================

  @Test
  public void nodesPreserveOrder() throws IOException {
    IntLiteral il1 = new IntLiteral(1, "int_1");
    IntLiteral il2 = new IntLiteral(2, "int_2");
    Sum sum1 = new Sum(il1, il2, null);
    IntLiteral il3 = new IntLiteral(3, "int_3");
    IntLiteral il4 = new IntLiteral(4, "int_4");
    Sum sum2 = new Sum(il3, il4, null);
    LionBinSerialization s = prepareForSimpleMath();
    byte[] bytes = s.serializeTreesToByteArray(sum1, sum2);
    List<Node> result = s.deserializeToNodes(bytes);
    assertEquals(Arrays.asList(sum1, il1, il2, sum2, il3, il4), result);
  }

  @Test
  public void arbitrarySerializeOrderPreserved() throws IOException {
    IntLiteral il1 = new IntLiteral(1, "int_1");
    IntLiteral il2 = new IntLiteral(2, "int_2");
    Sum sum1 = new Sum(il1, il2, null);
    IntLiteral il3 = new IntLiteral(3, "int_3");
    IntLiteral il4 = new IntLiteral(4, "int_4");
    Sum sum2 = new Sum(il3, il4, null);
    LionBinSerialization s = prepareForSimpleMath();
    byte[] bytes = s.serializeNodesToByteArray(Arrays.asList(il4, il1, sum1, il2, sum2, il3));
    List<Node> result = s.deserializeToNodes(bytes);
    assertEquals(Arrays.asList(il4, il1, sum1, il2, sum2, il3), result);
  }

  // =========================================================================
  // Nodes without IDs
  // =========================================================================

  @Test
  public void nodesWithoutIDsPreserveOrder() throws IOException {
    IntLiteral il1 = new IntLiteral(1, null);
    IntLiteral il2 = new IntLiteral(2, null);
    IntLiteral il3 = new IntLiteral(3, null);
    LionBinSerialization s = prepareForSimpleMath();
    byte[] bytes = s.serializeTreesToByteArray(il1, il2, il3);
    List<IntLiteral> result =
        s.deserializeToNodes(bytes).stream()
            .map(n -> (IntLiteral) n)
            .collect(Collectors.toList());
    assertEquals(Arrays.asList(il1, il2, il3), result);
  }

  // =========================================================================
  // Child with null ID → DeserializationException
  // =========================================================================

  @Test
  public void childWithNullIDThrows() throws IOException {
    IntLiteral il1 = new IntLiteral(1, "int_1");
    IntLiteral il2 = new IntLiteral(2, null);
    Sum sum1 = new Sum(il1, il2, null);
    LionBinSerialization s = prepareForSimpleMath();
    byte[] bytes = s.serializeNodesToByteArray(Arrays.asList(sum1, il1, il2));
    assertThrows(DeserializationException.class, () -> s.deserializeToNodes(bytes));
  }

  // =========================================================================
  // References
  // =========================================================================

  @Test
  public void referencesRoundTrip() throws IOException {
    RefNode r1 = new RefNode("r1");
    RefNode r2 = new RefNode("r2");
    RefNode r3 = new RefNode("r3");
    r1.setReferred(r2);
    r2.setReferred(r3);
    r3.setReferred(r1);
    LionBinSerialization s = prepareForRefs();
    byte[] bytes = s.serializeNodesToByteArray(Arrays.asList(r1, r2, r3));
    List<Node> result = s.deserializeToNodes(bytes);
    assertEquals(3, result.size());
    // equals() on RefNode compares referred.getID(), so this validates the reference wiring
    assertEquals(r1, result.get(0));
    assertEquals(r2, result.get(1));
    assertEquals(r3, result.get(2));
  }

  // =========================================================================
  // Chunk round-trip: LionBin chunk matches ProtoBuf chunk for same nodes
  // =========================================================================

  @Test
  public void chunkMatchesProtoBufChunk() throws IOException {
    Sum sum1 = new Sum(new IntLiteral(1), new IntLiteral(2));
    Sum sum2 = new Sum(new IntLiteral(3), new IntLiteral(4));

    LionBinSerialization lb = SerializationProvider.getStandardLionBinSerialization();
    ProtoBufSerialization pb = SerializationProvider.getStandardProtoBufSerialization();

    SerializationChunk lbChunk = lb.deserializeToChunk(lb.serializeTreesToByteArray(sum1, sum2));
    SerializationChunk pbChunk = pb.deserializeToChunk(pb.serializeTreesToByteArray(sum1, sum2));

    // Same serialization format version
    assertEquals(pbChunk.getSerializationFormatVersion(), lbChunk.getSerializationFormatVersion());

    // Same node count and same IDs in same order
    List<SerializedClassifierInstance> lbNodes = lbChunk.getClassifierInstances();
    List<SerializedClassifierInstance> pbNodes = pbChunk.getClassifierInstances();
    assertEquals(pbNodes.size(), lbNodes.size());
    for (int i = 0; i < pbNodes.size(); i++) {
      assertEquals(pbNodes.get(i).getID(), lbNodes.get(i).getID(), "Node ID mismatch at index " + i);
      assertEquals(
          pbNodes.get(i).getClassifier(),
          lbNodes.get(i).getClassifier(),
          "Classifier mismatch at index " + i);
    }
  }

  // =========================================================================
  // Magic bytes / version mismatch
  // =========================================================================

  @Test
  public void badMagicThrows() {
    byte[] garbage = "not a lionbin stream".getBytes();
    assertThrows(DeserializationException.class, () -> new LionBinSerialization().deserializeToChunk(garbage));
  }
}
