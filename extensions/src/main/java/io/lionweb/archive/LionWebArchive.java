package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.language.Language;
import io.lionweb.model.Node;
import io.lionweb.serialization.*;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Represents an LionWeb Archive.
 *
 * <p>The LionWeb Archive is a container file used to package a set or related LionWeb partitions
 * (e.g., the content of a LionWeb Repository) in a single archive. It is technically a ZIP file
 * that can have any extension, although the standard extension is <code>.lwa</code> (for
 * <em>LionWeb Archive</em>).
 *
 * <p>The archive follows a predefined directory structure containing the following directories:
 *
 * <ul>
 *   <li><b>Partitions/</b> — Contains a collection of LionWeb partitions.
 *   <li><b>Languages/</b> — Contains the language definitions required to load and interpret the
 *       partitions.
 *   <li><b>Metadata/</b> — Contains metadata files describing the archive. This directory always
 *       includes a file named <code>Metadata.properties</code>, which defines the archive’s
 *       properties.
 * </ul>
 *
 * <p>One of the properties defined in <code>Metadata.properties</code> is <code>LionWebVersion
 * </code>, which indicates the version of the LionWeb format used.
 *
 * <p>All files contained in the LionWeb Archive are serialized using the <b>Protocol Buffers
 * (Protobuf)</b> format.
 */
public class LionWebArchive {

  public static void load(
      File file, LionWebVersion lionWebVersion, Consumer<SerializationChunk> chunkConsumer)
      throws IOException {
    if (!file.exists()) {
      throw new IllegalArgumentException("The given file does not exist");
    }
    if (file.isDirectory()) {
      throw new IllegalArgumentException("The given file is a directory");
    }
    ProtoBufSerialization serialization =
        SerializationProvider.getStandardProtoBufSerialization(lionWebVersion);

    final int BUF_4MiB = 4 << 20;
    try (InputStream fileIn =
            new BufferedInputStream(Files.newInputStream(file.toPath()), BUF_4MiB);
        ZipInputStream zipIn = new ZipInputStream(fileIn)) {
      ZipEntry entry = zipIn.getNextEntry();
      while (entry != null) {
        if (entry.isDirectory()) {
          throw new IllegalArgumentException("Entry is a directory: " + entry.getName());
        }

        try {
          SerializationChunk chunk;
          byte[] bytes = readAllBytes(zipIn);
          chunk = serialization.deserializeToChunk(bytes);
          zipIn.closeEntry();
          chunkConsumer.accept(chunk);
        } catch (Exception e) {
          throw new RuntimeException("Failed to deserialize chunk from " + entry.getName(), e);
        }
        entry = zipIn.getNextEntry();
      }
    }
  }

  public static void load(
      File file, InMemoryServer server, String repositoryName, LionWebVersion lionWebVersion)
      throws IOException {
    load(
        file,
        lionWebVersion,
        chunk -> {
          server.createPartitionFromChunk(repositoryName, chunk.getClassifierInstances());
        });
  }

  public static void loadNodes(
      File file, ProtoBufSerialization protoBufSerialization, Consumer<Node> nodeConsumer)
      throws IOException {
    load(
        file,
        protoBufSerialization.getLionWebVersion(),
        chunk -> {
          Node root = (Node) protoBufSerialization.deserializeSerializationChunk(chunk).get(0);
          nodeConsumer.accept(root);
        });
  }

  public static List<Node> load(File file, ProtoBufSerialization protoBufSerialization)
      throws IOException {
    List<Node> nodes = new ArrayList<>();
    loadNodes(file, protoBufSerialization, nodes::add);
    return nodes;
  }

  public static List<Node> loadSelfLoadingLanguages(
      File file, ProtoBufSerialization protoBufSerialization) throws IOException {
    List<SerializationChunk> chunks = new ArrayList<>();
    load(
        file,
        protoBufSerialization.getLionWebVersion(),
        (Consumer<SerializationChunk>)
            serializationChunk -> {
              chunks.add(serializationChunk);
            });
    TopologicalSorter topologicalSorter =
        new TopologicalSorter(protoBufSerialization.getLionWebVersion());
    List<SerializationChunk> languageChunks = topologicalSorter.topologicalSort(chunks);
    languageChunks.forEach(
        languageChunk -> {
          Language language =
              (Language) protoBufSerialization.deserializeSerializationChunk(languageChunk).get(0);
          protoBufSerialization.registerLanguage(language);
        });
    List<Node> nodes = new ArrayList<>();
    chunks.forEach(
        serializationChunk -> {
          nodes.add(
              (Node)
                  protoBufSerialization.deserializeSerializationChunk(serializationChunk).get(0));
        });
    return nodes;
  }

  private static final int FOUR_MIB = 4 << 20;

  public static void store(
      File file, InMemoryServer server, String repositoryName, LionWebVersion lionWebVersion)
      throws IOException {
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
    ExecutorService pool =
        Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()));
    List<Future<Map.Entry<String, byte[]>>> futures = new ArrayList<>(nPartitions);
    ProtoBufSerialization serialization = new ProtoBufSerialization();

    for (int i = 0; i < nPartitions; i++) {
      final String partitionId = partitionIds.get(i);
      futures.add(
          pool.submit(
              () -> {
                SerializationChunk chunk =
                    SerializationChunk.fromNodes(
                        lionWebVersion,
                        server.retrieve(
                            repositoryName,
                            Collections.singletonList(partitionId),
                            Integer.MAX_VALUE));
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
    try (OutputStream os =
            new BufferedOutputStream(
                Files.newOutputStream(
                    file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
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
}
