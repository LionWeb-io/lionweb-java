package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Language;
import io.lionweb.model.Node;
import io.lionweb.serialization.*;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;

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
 *   <li><b>partitions/</b> — Contains a collection of LionWeb partitions.
 *   <li><b>languages/</b> — Contains the language definitions required to load and interpret the
 *       partitions.
 *   <li><b>metadata/</b> — Contains metadata files describing the archive. This directory always
 *       includes a file named <code>Metadata.properties</code>, which defines the archive’s
 *       properties.
 * </ul>
 *
 * <p>One of the properties defined in <code>metadata.properties</code> is <code>LionWeb-Version
 * </code>, which indicates the version of the LionWeb format used.
 *
 * <p>All files contained in the LionWeb Archive are serialized using the <b>Protocol Buffers
 * (Protobuf)</b> format.
 */
public class LionWebArchive {

  public static final String LW_VERION_KEY = "LionWeb-Version";

  private LionWebArchive() {}

  interface Loader {
    void setLwVersion(LionWebVersion lionWebVersion);

    void addLanguageChunk(SerializationChunk chunk);

    void languagesLoaded();

    void addPartitionChunk(SerializationChunk chunk);

    void partitionsLoaded();
  }

  /**
   * Loads a LionWeb archive from the specified file. This method extracts metadata, language
   * chunks, and partition chunks from the archive and processes them using the provided consumer
   * functions.
   *
   * <p>The archive is expected to contain a metadata file ("metadata/metadata.properties"),
   * language data under the "languages/" path, and partition data under the "partitions/" path.
   *
   * @param file the LionWeb archive file to be loaded. Must not be null, a directory, or
   *     non-existent.
   * @param loader the consumer function to be called for each language and partition chunk.
   * @throws IOException if an error occurs reading the file or processing its contents.
   * @throws IllegalArgumentException if the file does not exist, is a directory, or the archive
   *     does not meet the required structural assumptions.
   * @throws RuntimeException if an error occurs during chunk deserialization.
   */
  public static void load(File file, Loader loader) throws IOException {
    if (!file.exists()) {
      throw new IllegalArgumentException("The given file does not exist");
    }
    if (file.isDirectory()) {
      throw new IllegalArgumentException("The given file is a directory");
    }

    final int BUF_4MiB = 4 << 20;
    Properties metadata = null;
    String metadataPath = "metadata/metadata.properties";

    // First pass: read Metadata/Metadata.properties
    try (InputStream fileIn =
            new BufferedInputStream(Files.newInputStream(file.toPath()), BUF_4MiB);
        ZipInputStream zipIn = new ZipInputStream(fileIn)) {

      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        if (!entry.isDirectory() && entry.getName().equalsIgnoreCase(metadataPath)) {
          metadata = new Properties();
          metadata.load(zipIn);
          zipIn.closeEntry();
          break; // stop after reading metadata
        }
        zipIn.closeEntry();
      }
    }

    if (metadata == null) {
      throw new IllegalArgumentException("No Metadata.properties file found in archive");
    }
    if (!metadata.containsKey(LW_VERION_KEY)) {
      throw new IllegalArgumentException(
          "No " + LW_VERION_KEY + " property found in metadata.properties");
    }
    LionWebVersion lionWebVersion = LionWebVersion.fromValue(metadata.getProperty(LW_VERION_KEY));
    loader.setLwVersion(lionWebVersion);
    ProtoBufSerialization serialization =
        SerializationProvider.getStandardProtoBufSerialization(lionWebVersion);

    // Languages
    try (InputStream fileIn =
            new BufferedInputStream(Files.newInputStream(file.toPath()), BUF_4MiB);
        ZipInputStream zipIn = new ZipInputStream(fileIn)) {
      ZipEntry entry = zipIn.getNextEntry();
      while (entry != null) {
        if (entry.isDirectory()) {
          throw new IllegalArgumentException("Entry is a directory: " + entry.getName());
        }
        if (entry.getName().startsWith("languages/")) {
          try {
            SerializationChunk chunk;
            byte[] bytes = readAllBytes(zipIn);
            chunk = serialization.deserializeToChunk(bytes);
            zipIn.closeEntry();
            loader.addLanguageChunk(chunk);
          } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize chunk from " + entry.getName(), e);
          }
        }
        entry = zipIn.getNextEntry();
      }
    }
    loader.languagesLoaded();

    // Partitions
    try (InputStream fileIn =
            new BufferedInputStream(Files.newInputStream(file.toPath()), BUF_4MiB);
        ZipInputStream zipIn = new ZipInputStream(fileIn)) {
      ZipEntry entry = zipIn.getNextEntry();
      while (entry != null) {
        if (entry.isDirectory()) {
          throw new IllegalArgumentException("Entry is a directory: " + entry.getName());
        }
        if (entry.getName().startsWith("partitions/")) {
          try {
            SerializationChunk chunk;
            byte[] bytes = readAllBytes(zipIn);
            chunk = serialization.deserializeToChunk(bytes);
            zipIn.closeEntry();
            loader.addPartitionChunk(chunk);
          } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize chunk from " + entry.getName(), e);
          }
        }
        entry = zipIn.getNextEntry();
      }
    }
    loader.partitionsLoaded();
  }

  public static void loadNodes(
      File file, ProtoBufSerialization protoBufSerialization, Consumer<Node> partitionConsumer)
      throws IOException {

    class MyLoader implements Loader {
      List<SerializationChunk> languageChunks = new ArrayList<>();
      TopologicalSorter topologicalSorter = null;

      public void setLwVersion(LionWebVersion lionWebVersion) {
        topologicalSorter = new TopologicalSorter(lionWebVersion);
      }

      public void addLanguageChunk(SerializationChunk chunk) {
        languageChunks.add(chunk);
      }

      @Override
      public void languagesLoaded() {
        languageChunks = topologicalSorter.topologicalSort(languageChunks);
        languageChunks.forEach(
            languageChunk -> {
              Language language =
                  (Language)
                      protoBufSerialization.deserializeSerializationChunk(languageChunk).get(0);
              protoBufSerialization.registerLanguage(language);
            });
      }

      public void addPartitionChunk(SerializationChunk chunk) {
        partitionConsumer.accept(
            (Node) protoBufSerialization.deserializeSerializationChunk(chunk).get(0));
      }

      @Override
      public void partitionsLoaded() {}
    }

    load(file, new MyLoader());
  }

  public static List<Node> loadNodes(File file, ProtoBufSerialization protoBufSerialization)
      throws IOException {
    List<Node> nodes = new ArrayList<>();
    loadNodes(file, protoBufSerialization, nodes::add);
    return nodes;
  }

  private static final int FOUR_MIB = 4 << 20;

  /**
   * Store a LionWeb archive.
   *
   * <p>This implementation pre-serializes all language and partition chunks in parallel using a
   * thread pool, then writes the resulting byte arrays sequentially into the ZIP. This makes
   * store() significantly faster on multi-core machines, while preserving archive structure
   * and behavior.
   */
  public static void store(
      File file,
      LionWebVersion lionWebVersion,
      Iterable<SerializationChunk> languageChunks,
      Iterable<SerializationChunk> partitionChunks)
      throws IOException {

    // Ensure parent directory exists
    File parent = file.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }

    // Store metadata
    Properties metadata = new Properties();
    metadata.setProperty(LW_VERION_KEY, lionWebVersion.getVersionString());

    // Pre-serialize chunks in parallel
    List<ZipChunk> languageEntries =
        serializeChunksToZipEntries(lionWebVersion, languageChunks, "languages");
    List<ZipChunk> partitionEntries =
        serializeChunksToZipEntries(lionWebVersion, partitionChunks, "partitions");

    // Write zip sequentially
    try (OutputStream os =
            new BufferedOutputStream(
                Files.newOutputStream(
                    file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                FOUR_MIB);
        ZipOutputStream zipOut = new ZipOutputStream(os)) {

      // Favour speed; size is usually secondary for this use case
      zipOut.setLevel(0);

      ZipEntry entry = new ZipEntry("metadata/metadata.properties");
      zipOut.putNextEntry(entry);
      metadata.store(zipOut, null);
      zipOut.closeEntry();

      for (ZipChunk zc : languageEntries) {
        ZipEntry e = new ZipEntry(zc.entryName);
        zipOut.putNextEntry(e);
        zipOut.write(zc.bytes);
        zipOut.closeEntry();
      }

      for (ZipChunk zc : partitionEntries) {
        ZipEntry e = new ZipEntry(zc.entryName);
        zipOut.putNextEntry(e);
        zipOut.write(zc.bytes);
        zipOut.closeEntry();
      }
    }
  }

  /**
   * Simple holder for a prepared ZIP entry.
   */
  private static final class ZipChunk {
    final String entryName;
    final byte[] bytes;

    ZipChunk(String entryName, byte[] bytes) {
      this.entryName = entryName;
      this.bytes = bytes;
    }
  }

  /**
   * Serialize all chunks into byte[] in parallel and prepare their ZIP entry names.
   *
   * <p>Order of the returned list matches the iteration order of the input iterable.
   */
  private static List<ZipChunk> serializeChunksToZipEntries(
      final LionWebVersion lionWebVersion,
      Iterable<SerializationChunk> chunks,
      final String dirPrefix) {

    // Materialize iterable to a list so we can parallelize deterministically.
    final List<SerializationChunk> list = new ArrayList<SerializationChunk>();
    for (SerializationChunk c : chunks) {
      list.add(c);
    }
    final int size = list.size();
    if (size == 0) {
      return Collections.emptyList();
    }

    // For a single chunk, avoid thread pool overhead.
    if (size == 1) {
      SerializationChunk chunk = list.get(0);
      String partitionId = getPartitionId(chunk);
      String entryName = dirPrefix + "/" + partitionId + ".binpb";
      ProtoBufSerialization ser =
          SerializationProvider.getStandardProtoBufSerialization(lionWebVersion);
      try {
        byte[] data = ser.serializeToByteArray(chunk);
        List<ZipChunk> result = new ArrayList<ZipChunk>(1);
        result.add(new ZipChunk(entryName, data));
        return result;
      } catch (Exception e) {
        throw new RuntimeException("Failed to serialize chunk for " + entryName, e);
      }
    }

    int availableProcessors = Runtime.getRuntime().availableProcessors();
    int threads = Math.max(1, Math.min(availableProcessors, size));

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    try {
      List<Future<ZipChunk>> futures = new ArrayList<Future<ZipChunk>>(size);

      for (int i = 0; i < size; i++) {
        final SerializationChunk chunk = list.get(i);
        final String partitionId = getPartitionId(chunk);
        final String entryName = dirPrefix + "/" + partitionId + ".binpb";

        futures.add(
            executor.submit(
                new Callable<ZipChunk>() {
                  @Override
                  public ZipChunk call() {
                    ProtoBufSerialization ser =
                        SerializationProvider.getStandardProtoBufSerialization(lionWebVersion);
                    try {
                      byte[] data = ser.serializeToByteArray(chunk);
                      return new ZipChunk(entryName, data);
                    } catch (Exception e) {
                      throw new RuntimeException(
                          "Failed to serialize chunk for " + entryName, e);
                    }
                  }
                }));
      }

      List<ZipChunk> result = new ArrayList<ZipChunk>(size);
      for (Future<ZipChunk> f : futures) {
        try {
          result.add(f.get());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Interrupted during chunk serialization", e);
        } catch (ExecutionException e) {
          Throwable cause = e.getCause();
          if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
          }
          throw new RuntimeException("Failed during chunk serialization", cause);
        }
      }
      return result;
    } finally {
      executor.shutdown();
    }
  }

  /**
   * Extracts the root partition id from a chunk (the classifier instance with no parent).
   */
  private static String getPartitionId(SerializationChunk chunk) {
    for (SerializedClassifierInstance n : chunk.getClassifierInstances()) {
      if (n.getParentNodeID() == null) {
        return n.getID();
      }
    }
    throw new IllegalStateException("No root classifier instance found in chunk");
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
