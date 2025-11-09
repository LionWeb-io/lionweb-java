package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.model.Node;
import io.lionweb.serialization.*;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.extensions.TransferFormat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class LionWebArchive {

    public static void load(File file, LionWebVersion lionWebVersion,
                            TransferFormat format, Consumer<SerializationChunk> chunkConsumer) throws IOException {
        if (!file.exists()) {
            throw new IllegalArgumentException("The given file does not exist");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("The given file is a directory");
        }
        AbstractSerialization serialization;
        LowLevelJsonSerialization lowLevelJsonSerialization = null;
        if (format == TransferFormat.JSON) {
            serialization = SerializationProvider.getStandardJsonSerialization(lionWebVersion);
            lowLevelJsonSerialization = new LowLevelJsonSerialization();
        } else if (format == TransferFormat.PROTOBUF) {
            serialization = SerializationProvider.getStandardProtoBufSerialization(lionWebVersion);
        } else {
            throw new UnsupportedOperationException("Unsupported serialization format: " + format);
        }

        final int BUF_4MiB = 4 << 20;
        try (InputStream fileIn = new BufferedInputStream(Files.newInputStream(file.toPath()), BUF_4MiB);
             ZipInputStream zipIn = new ZipInputStream(fileIn)) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                if (entry.isDirectory()) {
                    throw new IllegalArgumentException("Entry is a directory: " + entry.getName());
                }

                try {
                    SerializationChunk chunk;
                    if (format == TransferFormat.JSON) {
                        String json = readZipEntryAsString(zipIn, entry);
                        chunk = lowLevelJsonSerialization.deserializeSerializationBlock(json);
                    } else if (format == TransferFormat.PROTOBUF) {
                        byte[] bytes = readAllBytes(zipIn);
                        chunk = ((ProtoBufSerialization) serialization).deserializeToChunk(bytes);
                    } else {
                        throw new UnsupportedOperationException("Unsupported serialization format: " + format);
                    }
                    zipIn.closeEntry();
                    chunkConsumer.accept(chunk);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize chunk from " + entry.getName(), e);
                }
                entry = zipIn.getNextEntry();
            }
        }
    }

    public static void load(File file, InMemoryServer server, String repositoryName,
                            LionWebVersion lionWebVersion,
                            TransferFormat format) throws IOException {
        load(file, lionWebVersion, format, chunk -> {
            server.createPartitionFromChunk(repositoryName, chunk.getClassifierInstances());
        });
    }

    public static void load(File file, JsonSerialization jsonSerialization) throws IOException {
        load(file, jsonSerialization.getLionWebVersion(), TransferFormat.JSON, chunk -> {
            throw new UnsupportedOperationException();
        });
    }

    public static void load(File file, ProtoBufSerialization protoBufSerialization, Consumer<Node> nodeConsumer) throws IOException {
        load(file, protoBufSerialization.getLionWebVersion(), TransferFormat.PROTOBUF, chunk -> {
            Node root = (Node)protoBufSerialization.deserializeSerializationChunk(chunk).get(0);
            nodeConsumer.accept(root);
        });
    }

    public static List<Node> load(File file, ProtoBufSerialization protoBufSerialization) throws IOException {
        List<Node> nodes = new ArrayList<>();
        load(file, protoBufSerialization, nodes::add);
        return nodes;
    }

    private static final int FOUR_MIB = 4 << 20;

    public static void store(File file, InMemoryServer server, String repositoryName,
                             LionWebVersion lionWebVersion,
                             TransferFormat format) throws IOException {
        // Ensure parent directory exists
        File parent = file.getParentFile();
        if (parent != null) {
            // mkdirs() returns false if it already exists; that's fine
            parent.mkdirs();
        }

        // Gather partitions
        List<String> partitionIds = server.listPartitionIDs(repositoryName);
        final int nPartitions = partitionIds.size();

        // Serialize partitions in parallel
        ExecutorService pool = Executors.newFixedThreadPool(
                Math.max(1, Runtime.getRuntime().availableProcessors()));
        if (format == TransferFormat.JSON) {
            throw new UnsupportedOperationException("JSON serialization not yet implemented");
        } else if (format == TransferFormat.PROTOBUF) {
            List<Future<Map.Entry<String, byte[]>>> futures = new ArrayList<>(nPartitions);
            ProtoBufSerialization serialization = new ProtoBufSerialization();

            for (int i = 0; i < nPartitions; i++) {
                final String partitionId = partitionIds.get(i);
                futures.add(pool.submit(() -> {
                    SerializationChunk chunk = SerializationChunk.fromNodes(lionWebVersion, server.retrieve(repositoryName, Collections.singletonList(partitionId), Integer.MAX_VALUE));
                    byte[] bytes = serialization.serializeToByteArray(chunk);
                    return new AbstractMap.SimpleEntry<>(partitionId, bytes);
                }));
            }

            // Collect serialized partitions
            List<Map.Entry<String, byte[]>> serializedPartitions = new ArrayList<>(nPartitions);
            for (Future<Map.Entry<String, byte[]>> f : futures) {
                try {
                    serializedPartitions.add(f.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            pool.shutdown();

            // Write zip sequentially
            try (OutputStream os = new BufferedOutputStream(
                    Files.newOutputStream(file.toPath(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING),
                    FOUR_MIB);
                 ZipOutputStream zipOut = new ZipOutputStream(os)) {

                zipOut.setLevel(1);

                for (Map.Entry<String, byte[]> e : serializedPartitions) {
                    String partitionId = e.getKey();
                    byte[] bytes = e.getValue();

                    ZipEntry entry = new ZipEntry(partitionId + ".binpb");
                    zipOut.putNextEntry(entry);
                    zipOut.write(bytes);
                    zipOut.closeEntry();
                }
            }
        } else {
            throw new UnsupportedOperationException();
        }

    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
        byte[] buf = new byte[64 * 1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static String readZipEntryAsString(InputStream zipInputStream, ZipEntry entry) throws IOException {
        if (entry.isDirectory()) {
            throw new IllegalArgumentException("Cannot read a directory entry: " + entry.getName());
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = zipInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
