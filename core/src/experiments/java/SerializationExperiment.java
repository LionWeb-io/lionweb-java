import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.FlatBuffersSerialization;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.LowLevelJsonSerialization;
import io.lionweb.lioncore.java.serialization.ProtoBufSerialization;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.java.utils.ModelComparator;

import java.io.*;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SerializationExperiment {

    private static byte[] compress(String str) {
        if (str == null || str.length() == 0) {
            throw new RuntimeException();
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
            gzip.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String decompress(byte[] compressedData) {
        if (compressedData == null || compressedData.length == 0) {
            throw new RuntimeException();
        }

        StringBuilder outStr = new StringBuilder();
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, "UTF-8");
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outStr.append(line);
            }
            return outStr.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) throws IOException {
        TreeGenerator treeGenerator = new TreeGenerator(1);
        Node tree = treeGenerator.generate(150_000);
        System.out.println("Tree generated");

        SerializedChunk chunk = JsonSerialization.getStandardSerialization().serializeTreeToSerializationBlock(tree);

        System.out.println("= JSON serialization (without compression) =");
        long jt0 = System.currentTimeMillis();
        String json = new LowLevelJsonSerialization().serializeToJsonString(chunk);
        long jt1 = System.currentTimeMillis();
        System.out.println("  serialized in " + (jt1 - jt0) + "ms");
        System.out.println("  size " + json.getBytes().length + " bytes");
//        Node jUnserializedTree = jsonSerialization.deserializeToNodes(json).get(0);
//        long jt2 = System.currentTimeMillis();
//        System.out.println("  unserialized in " + (jt2 - jt1) + "ms");
//        assertInstancesAreEquals(tree, jUnserializedTree);

        System.out.println("= JSON serialization (with compression) =");
        long ct0 = System.currentTimeMillis();
        byte[] compressed = compress(new LowLevelJsonSerialization().serializeToJsonString(chunk));
        long ct1 = System.currentTimeMillis();
        System.out.println("  serialized in " + (ct1 - ct0) + "ms");
        System.out.println("  size " + compressed.length + " bytes");
//        Node cUnserializedTree = jsonSerializationCompress.deserializeToNodes(decompress(compressed)).get(0);
//        long ct2 = System.currentTimeMillis();
//        System.out.println("  unserialized in " + (ct2 - ct1) + "ms");
//        assertInstancesAreEquals(tree, cUnserializedTree);

        System.out.println("= ProtoBuf serialization =");
        long pt0 = System.currentTimeMillis();
        ProtoBufSerialization protoBufSerialization = ProtoBufSerialization.getStandardSerialization();
        protoBufSerialization.enableDynamicNodes();
        byte[] bytes = protoBufSerialization.serializeToByteArray(chunk);
        long pt1 = System.currentTimeMillis();
        System.out.println("  serialized in " + (pt1 - pt0) + "ms");
        System.out.println("  size " + bytes.length + " bytes");
//        Node pUnserializedTree = protoBufSerialization.deserializeToNodes(bytes).get(0);
//        long pt2 = System.currentTimeMillis();
//        System.out.println("  unserialized in " + (pt2 - pt1) + "ms");
//        assertInstancesAreEquals(tree, pUnserializedTree);

        System.out.println("= Flatbuffers serialization =");
        long ft0 = System.currentTimeMillis();
        FlatBuffersSerialization flatBuffersSerialization = FlatBuffersSerialization.getStandardSerialization();
        flatBuffersSerialization.enableDynamicNodes();
        byte[] fbytes = flatBuffersSerialization.serialize(chunk);
        long ft1 = System.currentTimeMillis();
        System.out.println("  serialized in " + (ft1 - ft0) + "ms");
        System.out.println("  size " + fbytes.length + " bytes");
//        Node pUnserializedTree = protoBufSerialization.deserializeToNodes(bytes).get(0);
//        long pt2 = System.currentTimeMillis();
//        System.out.println("  unserialized in " + (pt2 - pt1) + "ms");
//        assertInstancesAreEquals(tree, pUnserializedTree);

        System.out.println("= Comparison (protobuf against uncompressed JSON)=");
        {
            double serializationTimeRatio = ((double) (pt1 - pt0) * 100) / (jt1 - jt0);
            //double deserializationTimeRatio = ((double) (pt2 - pt1) * 100) / (jt2 - jt1);
            double sizeRatio = ((double) (bytes.length) * 100) / (json.getBytes().length);
            System.out.println("  serialization time: " + String.format("%.2f", serializationTimeRatio) + "%");
            //System.out.println("  deserialization time: " + String.format("%.2f", deserializationTimeRatio) + "%");
            System.out.println("  size: " + String.format("%.2f", sizeRatio) + "%");
        }

        System.out.println("= Comparison (protobuf against compressed JSON)=");
        {
            double serializationTimeRatio = ((double) (pt1 - pt0) * 100) / (ct1 - ct0);
            //double deserializationTimeRatio = ((double) (pt2 - pt1) * 100) / (ct2 - ct1);
            double sizeRatio = ((double) (bytes.length) * 100) / (compressed.length);
            System.out.println("  serialization time: " + String.format("%.2f", serializationTimeRatio) + "%");
            //System.out.println("  deserialization time: " + String.format("%.2f", deserializationTimeRatio) + "%");
            System.out.println("  size: " + String.format("%.2f", sizeRatio) + "%");
        }

        System.out.println("= Comparison (flatbuffers against uncompressed JSON)=");
        {
            double serializationTimeRatio = ((double) (ft1 - ft0) * 100) / (jt1 - jt0);
            //double deserializationTimeRatio = ((double) (pt2 - pt1) * 100) / (jt2 - jt1);
            double sizeRatio = ((double) (fbytes.length) * 100) / (json.getBytes().length);
            System.out.println("  serialization time: " + String.format("%.2f", serializationTimeRatio) + "%");
            //System.out.println("  deserialization time: " + String.format("%.2f", deserializationTimeRatio) + "%");
            System.out.println("  size: " + String.format("%.2f", sizeRatio) + "%");
        }

        System.out.println("= Comparison (flatbuffers against compressed JSON)=");
        {
            double serializationTimeRatio = ((double) (ft1 - ft0) * 100) / (ct1 - ct0);
            //double deserializationTimeRatio = ((double) (pt2 - pt1) * 100) / (ct2 - ct1);
            double sizeRatio = ((double) (fbytes.length) * 100) / (compressed.length);
            System.out.println("  serialization time: " + String.format("%.2f", serializationTimeRatio) + "%");
            //System.out.println("  deserialization time: " + String.format("%.2f", deserializationTimeRatio) + "%");
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
