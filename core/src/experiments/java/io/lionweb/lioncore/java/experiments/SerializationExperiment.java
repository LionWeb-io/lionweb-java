package io.lionweb.lioncore.java.experiments;

import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.FlatBuffersSerialization;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.LowLevelJsonSerialization;
import io.lionweb.lioncore.java.serialization.ProtoBufSerialization;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;

public class SerializationExperiment {

  public static void main(String[] args) {
    TreeGenerator treeGenerator = new TreeGenerator(1);
    Node tree = treeGenerator.generate(500_000);
    System.out.println("Tree generated");

    SerializedChunk chunk =
        JsonSerialization.getStandardSerialization().serializeTreeToSerializationBlock(tree);

    System.out.println("= JSON serialization (without compression) =");
    long jt0 = System.currentTimeMillis();
    String json = new LowLevelJsonSerialization().serializeToJsonString(chunk);
    long jt1 = System.currentTimeMillis();
    System.out.println("  serialized in " + (jt1 - jt0) + "ms");
    System.out.println("  size " + json.getBytes().length + " bytes");

    System.out.println("= JSON serialization (with compression) =");
    long ct0 = System.currentTimeMillis();
    byte[] compressed =
        GZipFacade.compress(new LowLevelJsonSerialization().serializeToJsonString(chunk));
    long ct1 = System.currentTimeMillis();
    System.out.println("  serialized in " + (ct1 - ct0) + "ms");
    System.out.println("  size " + compressed.length + " bytes");

    System.out.println("= ProtoBuf serialization =");
    long pt0 = System.currentTimeMillis();
    ProtoBufSerialization protoBufSerialization = ProtoBufSerialization.getStandardSerialization();
    protoBufSerialization.enableDynamicNodes();
    byte[] bytes = protoBufSerialization.serializeToByteArray(chunk);
    long pt1 = System.currentTimeMillis();
    System.out.println("  serialized in " + (pt1 - pt0) + "ms");
    System.out.println("  size " + bytes.length + " bytes");

    System.out.println("= Flatbuffers serialization =");
    long ft0 = System.currentTimeMillis();
    FlatBuffersSerialization flatBuffersSerialization =
        FlatBuffersSerialization.getStandardSerialization();
    flatBuffersSerialization.enableDynamicNodes();
    byte[] fbytes = flatBuffersSerialization.serialize(chunk);
    long ft1 = System.currentTimeMillis();
    System.out.println("  serialized in " + (ft1 - ft0) + "ms");
    System.out.println("  size " + fbytes.length + " bytes");

    System.out.println("= Comparison (protobuf against uncompressed JSON)=");
    {
      double serializationTimeRatio = ((double) (pt1 - pt0) * 100) / (jt1 - jt0);
      double sizeRatio = ((double) (bytes.length) * 100) / (json.getBytes().length);
      System.out.println(
          "  serialization time: " + String.format("%.2f", serializationTimeRatio) + "%");
      System.out.println("  size: " + String.format("%.2f", sizeRatio) + "%");
    }

    System.out.println("= Comparison (protobuf against compressed JSON)=");
    {
      double serializationTimeRatio = ((double) (pt1 - pt0) * 100) / (ct1 - ct0);
      double sizeRatio = ((double) (bytes.length) * 100) / (compressed.length);
      System.out.println(
          "  serialization time: " + String.format("%.2f", serializationTimeRatio) + "%");
      System.out.println("  size: " + String.format("%.2f", sizeRatio) + "%");
    }

    System.out.println("= Comparison (flatbuffers against uncompressed JSON)=");
    {
      double serializationTimeRatio = ((double) (ft1 - ft0) * 100) / (jt1 - jt0);
      double sizeRatio = ((double) (fbytes.length) * 100) / (json.getBytes().length);
      System.out.println(
          "  serialization time: " + String.format("%.2f", serializationTimeRatio) + "%");
      System.out.println("  size: " + String.format("%.2f", sizeRatio) + "%");
    }

    System.out.println("= Comparison (flatbuffers against compressed JSON)=");
    {
      double serializationTimeRatio = ((double) (ft1 - ft0) * 100) / (ct1 - ct0);
      double sizeRatio = ((double) (fbytes.length) * 100) / (compressed.length);
      System.out.println(
          "  serialization time: " + String.format("%.2f", serializationTimeRatio) + "%");
      System.out.println("  size: " + String.format("%.2f", sizeRatio) + "%");
    }
  }
}
