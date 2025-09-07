package io.lionweb.experiments;

import static io.lionweb.experiments.GZipFacade.decompress;

import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.serialization.*;
import io.lionweb.serialization.data.SerializedChunk;
import io.lionweb.utils.ModelComparator;
import java.io.*;

public class SerializationAndDeserializationExperiment {

  public static void main(String[] args) throws IOException {
    long gen0 = System.currentTimeMillis();
    TreeGenerator treeGenerator = new TreeGenerator(1);
    Node tree = treeGenerator.generate(100_000);
    long gen1 = System.currentTimeMillis();
    System.out.println("Tree generated in " + (gen1 - gen0) + " ms");

    SerializedChunk chunk =
        SerializationProvider.getStandardJsonSerialization()
            .serializeTreeToSerializationChunk(tree);

    System.out.println("= JSON serialization (without compression) =");
    long jt0 = System.currentTimeMillis();
    String json = new LowLevelJsonSerialization().serializeToJsonString(chunk);
    long jt1 = System.currentTimeMillis();
    System.out.println("  serialized in " + (jt1 - jt0) + "ms");
    System.out.println("  size " + json.getBytes().length + " bytes");

    // Note that this method include the transformation from SerializedChunk to node,
    // which is common to all deserialization operations
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    jsonSerialization.enableDynamicNodes();
    jsonSerialization.registerLanguage(SimpleLanguage.language);
    Node jUnserializedTree = jsonSerialization.deserializeToNodes(json).get(0);
    long jt2 = System.currentTimeMillis();
    System.out.println("  unserialized in " + (jt2 - jt1) + "ms");
    assertInstancesAreEquals(tree, jUnserializedTree);

    System.out.println("= JSON serialization (with compression) =");
    long ct0 = System.currentTimeMillis();
    byte[] compressed =
        GZipFacade.compress(new LowLevelJsonSerialization().serializeToJsonString(chunk));
    long ct1 = System.currentTimeMillis();
    System.out.println("  serialized in " + (ct1 - ct0) + "ms");
    System.out.println("  size " + compressed.length + " bytes");

    // Note that this method include the transformation from SerializedChunk to node,
    // which is common to all deserialization operations
    JsonSerialization jsonSerializationCompress =
        SerializationProvider.getStandardJsonSerialization();
    jsonSerializationCompress.enableDynamicNodes();
    jsonSerializationCompress.registerLanguage(SimpleLanguage.language);
    Node cUnserializedTree =
        jsonSerializationCompress.deserializeToNodes(decompress(compressed)).get(0);
    long ct2 = System.currentTimeMillis();
    System.out.println("  unserialized in " + (ct2 - ct1) + "ms");
    assertInstancesAreEquals(tree, cUnserializedTree);

    System.out.println("= ProtoBuf serialization =");
    long pt0 = System.currentTimeMillis();
    ProtoBufSerialization protoBufSerialization =
        SerializationProvider.getStandardProtoBufSerialization();
    protoBufSerialization.enableDynamicNodes();
    byte[] bytes = protoBufSerialization.serializeToByteArray(chunk);
    long pt1 = System.currentTimeMillis();
    System.out.println("  serialized in " + (pt1 - pt0) + "ms");
    System.out.println("  size " + bytes.length + " bytes");

    // Note that this method include the transformation from SerializedChunk to node,
    // which is common to all deserialization operations
    ProtoBufSerialization protoBufSerializationForDeserialization =
        SerializationProvider.getStandardProtoBufSerialization();
    protoBufSerializationForDeserialization.registerLanguage(SimpleLanguage.language);
    protoBufSerializationForDeserialization.enableDynamicNodes();
    Node pUnserializedTree =
        protoBufSerializationForDeserialization.deserializeToNodes(bytes).get(0);
    long pt2 = System.currentTimeMillis();
    System.out.println("  unserialized in " + (pt2 - pt1) + "ms");
    assertInstancesAreEquals(tree, pUnserializedTree);

    System.out.println("= Comparison (protobuf against uncompressed JSON)=");
    {
      double serializationTimeRatio = ((double) (pt1 - pt0) * 100) / (jt1 - jt0);
      double deserializationTimeRatio = ((double) (pt2 - pt1) * 100) / (jt2 - jt1);
      double sizeRatio = ((double) (bytes.length) * 100) / (json.getBytes().length);
      System.out.println(
          "  serialization time: " + String.format("%.2f", serializationTimeRatio) + "%");
      System.out.println(
          "  deserialization time: " + String.format("%.2f", deserializationTimeRatio) + "%");
      System.out.println("  size: " + String.format("%.2f", sizeRatio) + "%");
    }

    System.out.println("= Comparison (protobuf against compressed JSON)=");
    {
      double serializationTimeRatio = ((double) (pt1 - pt0) * 100) / (ct1 - ct0);
      double deserializationTimeRatio = ((double) (pt2 - pt1) * 100) / (ct2 - ct1);
      double sizeRatio = ((double) (bytes.length) * 100) / (compressed.length);
      System.out.println(
          "  serialization time: " + String.format("%.2f", serializationTimeRatio) + "%");
      System.out.println(
          "  deserialization time: " + String.format("%.2f", deserializationTimeRatio) + "%");
      System.out.println("  size: " + String.format("%.2f", sizeRatio) + "%");
    }
  }

  private static void assertInstancesAreEquals(ClassifierInstance<?> a, ClassifierInstance<?> b) {
    ModelComparator modelComparator = new ModelComparator();
    ModelComparator.ComparisonResult comparisonResult = modelComparator.compare(a, b);
    if (!comparisonResult.areEquivalent()) {
      throw new RuntimeException(comparisonResult.toString());
    }
  }
}
