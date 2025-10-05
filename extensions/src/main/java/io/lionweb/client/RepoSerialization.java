package io.lionweb.client;

import static io.lionweb.serialization.LowLevelJsonSerialization.groupNodesIntoSerializationBlock;

import io.lionweb.client.api.BulkAPIClient;
import io.lionweb.client.api.JSONLevelBulkAPIClient;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.extensions.AdditionalAPIClient;
import io.lionweb.serialization.extensions.BulkImport;
import io.lionweb.serialization.extensions.Compression;
import io.lionweb.serialization.extensions.TransferFormat;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import kotlin.text.Charsets;

/** This class contains the logic to store and retrieve entire repositories at once. */
public class RepoSerialization {
  private int numberOfNodesThreshold = 100_000;
  private TransferFormat transferFormat = TransferFormat.PROTOBUF;
  private Compression compression = Compression.DISABLED;

  /**
   * Download all the content of the repository accessed by the apiClient into a directory. In this
   * directory one file per partition is created. The file is in JSON format.
   */
  public <C extends JSONLevelBulkAPIClient & BulkAPIClient> void downloadRepoAsDirectory(
      C apiClient, File directory) throws IOException {
    for (String partitionID : apiClient.listPartitionsIDs()) {
      String partitionData =
          apiClient.rawRetrieve(Collections.singletonList(partitionID), Integer.MAX_VALUE);
      File partitionFile = new File(directory, partitionID + ".json");
      Files.write(
          partitionFile.toPath(),
          Collections.singletonList(partitionData),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    }
  }

  /**
   * Download all the content of the repository accessed by the apiClient into a zip file. In this
   * zip file, one entry per partition is created. The entry is in JSON format.
   */
  public <C extends JSONLevelBulkAPIClient & BulkAPIClient> void downloadRepoAsZip(
      C apiClient, File zipFile) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(zipFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zos = new ZipOutputStream(bos)) {

      for (String partitionID : apiClient.listPartitionsIDs()) {
        String partitionData =
            apiClient.rawRetrieve(Collections.singletonList(partitionID), Integer.MAX_VALUE);

        ZipEntry entry = new ZipEntry(partitionID + ".json");
        zos.putNextEntry(entry);

        byte[] dataBytes = partitionData.getBytes(StandardCharsets.UTF_8);
        zos.write(dataBytes, 0, dataBytes.length);

        zos.closeEntry();
      }
    }
  }

  /**
   * Upload all the content of a directory to a given repository, using the standard bulk API (and
   * not the more performant bulk import). The directory and all the subdirectories are examined,
   * looking for files with extension ".json" (ignoring case).
   */
  public void simpleUploadDirectoryToRepo(JSONLevelBulkAPIClient apiClient, File directory)
      throws IOException {
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException(
          "Provided file is not a directory: " + directory.getAbsolutePath());
    }

    for (File file : findJsonFilesRecursively(directory)) {
      String content = new String(Files.readAllBytes(file.toPath()), Charsets.UTF_8);

      LowLevelJsonSerialization lowLevelJsonSerialization = new LowLevelJsonSerialization();
      SerializationChunk serializationChunk =
          lowLevelJsonSerialization.deserializeSerializationBlock(content);
      SerializedClassifierInstance root =
          serializationChunk.getClassifierInstances().stream()
              .filter(n -> n.getParentNodeID() == null)
              .findFirst()
              .get();
      root.clearContainments();
      SerializationChunk limitedSerializationChunk =
          groupNodesIntoSerializationBlock(
              Collections.singletonList(root), apiClient.getLionWebVersion());
      String limitedJson =
          lowLevelJsonSerialization.serializeToJsonString(limitedSerializationChunk);

      apiClient.rawCreatePartitions(limitedJson);

      apiClient.rawStore(content);
    }
  }

  /**
   * Upload all the content of a directory to a given repository, using the bulkImport operation
   * (and not the standard bulk operations). The directory and all the subdirectories are examined,
   * looking for files with extension ".json" (ignoring case).
   */
  public void uploadDirectoryToRepoUsingBulkImport(AdditionalAPIClient apiClient, File directory)
      throws IOException {
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException(
          "Provided file is not a directory: " + directory.getAbsolutePath());
    }

    LowLevelJsonSerialization lowLevelJsonSerialization = new LowLevelJsonSerialization();
    BulkImport bulkImport = new BulkImport();
    for (File file : findJsonFilesRecursively(directory)) {
      SerializationChunk serializationChunk =
          lowLevelJsonSerialization.deserializeSerializationBlock(file);
      bulkImport.addNodes(serializationChunk.getClassifierInstances());
      if (bulkImport.numberOfNodes() >= numberOfNodesThreshold) {
        apiClient.bulkImport(bulkImport, transferFormat, compression);
        bulkImport.clear();
      }
    }
    if (!bulkImport.isEmpty()) {
      apiClient.bulkImport(bulkImport, transferFormat, compression);
    }
  }

  /**
   * Upload all the content of a zip to a given repository, using the standard bulk API (and not the
   * more performant bulk import). All the zip is examined, looking for entries with extension
   * ".json" (ignoring case).
   */
  public void simpleUploadZipToRepo(JSONLevelBulkAPIClient apiClient, File zip) throws IOException {
    if (!zip.isFile()) {
      throw new IllegalArgumentException(
          "Provided path is not a valid zip file: " + zip.getAbsolutePath());
    }

    try (FileInputStream fis = new FileInputStream(zip);
        ZipInputStream zis = new ZipInputStream(fis, StandardCharsets.UTF_8)) {

      ZipEntry entry;
      final int BUFFER_SIZE = 32 * 1024;
      byte[] buffer = new byte[BUFFER_SIZE];
      while ((entry = zis.getNextEntry()) != null) {
        if (!entry.getName().toLowerCase().endsWith(".json")) {
          continue;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = zis.read(buffer)) != -1) {
          baos.write(buffer, 0, bytesRead);
        }

        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        LowLevelJsonSerialization lowLevelJsonSerialization = new LowLevelJsonSerialization();
        SerializationChunk serializationChunk =
            lowLevelJsonSerialization.deserializeSerializationBlock(content);
        SerializedClassifierInstance root =
            serializationChunk.getClassifierInstances().stream()
                .filter(n -> n.getParentNodeID() == null)
                .findFirst()
                .get();
        root.clearContainments();
        SerializationChunk limitedSerializationChunk =
            groupNodesIntoSerializationBlock(
                Collections.singletonList(root), apiClient.getLionWebVersion());
        String limitedJson =
            lowLevelJsonSerialization.serializeToJsonString(limitedSerializationChunk);

        apiClient.rawCreatePartitions(limitedJson);

        apiClient.rawStore(content);

        zis.closeEntry();
      }
    }
  }

  /**
   * Upload all the content of a zip to a given repository, using the bulkImport operation (and not
   * the standard bulk operations). All the zip is examined, looking for entries with extension
   * ".json" (ignoring case).
   */
  public void uploadZipToRepoUsingBulkImport(AdditionalAPIClient apiClient, File zip)
      throws IOException {
    if (!zip.isFile()) {
      throw new IllegalArgumentException(
          "Provided path is not a valid zip file: " + zip.getAbsolutePath());
    }

    final int BUFFER_SIZE = 32 * 1024;

    LowLevelJsonSerialization lowLevelJsonSerialization = new LowLevelJsonSerialization();
    BulkImport bulkImport = new BulkImport();

    try (FileInputStream fis = new FileInputStream(zip);
        ZipInputStream zis = new ZipInputStream(fis, StandardCharsets.UTF_8)) {

      ZipEntry entry;
      byte[] buffer = new byte[BUFFER_SIZE];

      while ((entry = zis.getNextEntry()) != null) {
        if (!entry.getName().toLowerCase().endsWith(".json")) {
          continue;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = zis.read(buffer)) != -1) {
          baos.write(buffer, 0, bytesRead);
        }

        // Write to temp file (needed because deserializeSerializationBlock expects a File)
        File tempFile = File.createTempFile("partition-", ".json");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
          baos.writeTo(fos);
        }

        SerializationChunk serializationChunk =
            lowLevelJsonSerialization.deserializeSerializationBlock(tempFile);
        bulkImport.addNodes(serializationChunk.getClassifierInstances());

        if (bulkImport.numberOfNodes() >= numberOfNodesThreshold) {
          apiClient.bulkImport(bulkImport, transferFormat, compression);
          bulkImport.clear();
        }

        zis.closeEntry();
      }
    }

    if (!bulkImport.isEmpty()) {
      apiClient.bulkImport(bulkImport, transferFormat, compression);
    }
  }

  private static List<File> findJsonFilesRecursively(File directory) throws IOException {
    try (Stream<Path> paths = Files.walk(directory.toPath())) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().toLowerCase().endsWith(".json"))
          .map(Path::toFile)
          .collect(Collectors.toList());
    }
  }
}
