package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Language;
import io.lionweb.model.Node;
import io.lionweb.serialization.*;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
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
    if (!metadata.containsKey("LionWeb-Version")) {
      throw new IllegalArgumentException(
          "No LionWeb-Version property found in Metadata.properties");
    }
    LionWebVersion lionWebVersion =
        LionWebVersion.fromValue(metadata.getProperty("LionWeb-Version"));
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

  private static final int FOUR_MIB = 4 << 20;

  public static void store(
      File file,
      LionWebVersion lionWebVersion,
      Stream<SerializationChunk> languageChunks,
      Stream<SerializationChunk> partitionChunks)
      throws IOException {
    // Ensure parent directory exists
    File parent = file.getParentFile();
    if (parent != null) {
      // mkdirs() returns false if it already exists; that's fine
      parent.mkdirs();
    }

    // Store metadata
    Properties metadata = new Properties();
    metadata.setProperty("version", lionWebVersion.getVersionString());

    ProtoBufSerialization protoBufSerialization =
        SerializationProvider.getStandardProtoBufSerialization(lionWebVersion);

    // Write zip sequentially
    try (OutputStream os =
            new BufferedOutputStream(
                Files.newOutputStream(
                    file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                FOUR_MIB);
        ZipOutputStream zipOut = new ZipOutputStream(os)) {

      zipOut.setLevel(1);

      ZipEntry entry = new ZipEntry("metadata/metadata.properties");
      zipOut.putNextEntry(entry);
      zipOut.write(metadata.toString().getBytes(StandardCharsets.UTF_8));
      zipOut.closeEntry();

      languageChunks.forEach(
          chunk -> {
            String partitionId =
                chunk.getClassifierInstances().stream()
                    .filter(n -> n.getParentNodeID() == null)
                    .map(n -> n.getID())
                    .findFirst()
                    .get();
            ZipEntry e = new ZipEntry("languages/" + partitionId + ".binpb");

            try {
              zipOut.putNextEntry(e);
              zipOut.write(protoBufSerialization.serializeToByteArray(chunk));
              zipOut.closeEntry();
            } catch (IOException ex) {
              throw new RuntimeException(ex);
            }
          });

      partitionChunks.forEach(
          chunk -> {
            String partitionId =
                chunk.getClassifierInstances().stream()
                    .filter(n -> n.getParentNodeID() == null)
                    .map(n -> n.getID())
                    .findFirst()
                    .get();
            ZipEntry e = new ZipEntry("partitions/" + partitionId + ".binpb");

            try {
              zipOut.putNextEntry(e);
              zipOut.write(protoBufSerialization.serializeToByteArray(chunk));
              zipOut.closeEntry();
            } catch (IOException ex) {
              throw new RuntimeException(ex);
            }
          });
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
