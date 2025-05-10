package io.lionweb.repoclient;

import io.lionweb.lioncore.java.serialization.LowLevelJsonSerialization;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.repoclient.api.BulkAPIClient;
import io.lionweb.serialization.extensions.AdditionalAPIClient;
import io.lionweb.serialization.extensions.BulkImport;
import io.lionweb.serialization.extensions.Compression;
import io.lionweb.serialization.extensions.TransferFormat;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import kotlin.text.Charsets;

public class RepoSerialization {
  private int nNodesThreshold = 100_000;
  private TransferFormat transferFormat = TransferFormat.FLATBUFFERS;
  private Compression compression = Compression.DISABLED;

  public void downloadRepoAsDirectory(BulkAPIClient bulkAPIClient, File directory)
      throws IOException {
    for (String partitionID : bulkAPIClient.listPartitionsIDs()) {
      String partitionData =
          bulkAPIClient.rawRetrieve(bulkAPIClient.listPartitionsIDs(), Integer.MAX_VALUE);
      File partitionFile = new File(directory, partitionID + ".json");
      Files.write(
          partitionFile.toPath(),
          Collections.singletonList(partitionData),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    }
  }

  public void downloadRepoAsZip(BulkAPIClient bulkAPIClient, File zipFile) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos)) {

      for (String partitionID : bulkAPIClient.listPartitionsIDs()) {
        String partitionData =
            bulkAPIClient.rawRetrieve(Collections.singletonList(partitionID), Integer.MAX_VALUE);

        ZipEntry entry = new ZipEntry(partitionID + ".json");
        zos.putNextEntry(entry);

        byte[] dataBytes = partitionData.getBytes(StandardCharsets.UTF_8);
        zos.write(dataBytes, 0, dataBytes.length);

        zos.closeEntry();
      }
    }
  }

  public void simpleUploadDirectoryToRepo(BulkAPIClient bulkAPIClient, File directory)
      throws IOException {
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException(
          "Provided file is not a directory: " + directory.getAbsolutePath());
    }

    File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
    if (files == null) {
      throw new IOException("Unable to list files in directory: " + directory.getAbsolutePath());
    }

    for (File file : files) {
      String content = new String(Files.readAllBytes(file.toPath()), Charsets.UTF_8);

      bulkAPIClient.rawStore(content);
    }
  }

  public void bulkUploadDirectoryToRepo(AdditionalAPIClient additionalAPIClient, File directory)
      throws IOException {
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException(
          "Provided file is not a directory: " + directory.getAbsolutePath());
    }

    File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
    if (files == null) {
      throw new IOException("Unable to list files in directory: " + directory.getAbsolutePath());
    }

    LowLevelJsonSerialization lowLevelJsonSerialization = new LowLevelJsonSerialization();
    BulkImport bulkImport = new BulkImport();
    for (File file : files) {
      SerializedChunk serializedChunk =
          lowLevelJsonSerialization.deserializeSerializationBlock(file);
      bulkImport.addNodes(serializedChunk.getClassifierInstances());
      if (bulkImport.numberOfNodes() >= nNodesThreshold) {
        additionalAPIClient.bulkImport(bulkImport, transferFormat, compression);
        bulkImport.clear();
      }
    }
    if (!bulkImport.isEmpty()) {
      additionalAPIClient.bulkImport(bulkImport, transferFormat, compression);
    }
  }

  public void simpleUploadZipToRepo(BulkAPIClient bulkAPIClient, File zip) throws IOException {
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
        if (!entry.getName().endsWith(".json")) {
          continue;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = zis.read(buffer)) != -1) {
          baos.write(buffer, 0, bytesRead);
        }

        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        System.out.println("STORE " + entry.getName());
        bulkAPIClient.rawStore(content);

        zis.closeEntry();
      }
    }
  }

  public void bulkUploadZipToRepo(AdditionalAPIClient additionalAPIClient, File zip)
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
        if (!entry.getName().endsWith(".json")) {
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

        SerializedChunk serializedChunk =
            lowLevelJsonSerialization.deserializeSerializationBlock(tempFile);
        bulkImport.addNodes(serializedChunk.getClassifierInstances());

        if (bulkImport.numberOfNodes() >= nNodesThreshold) {
          additionalAPIClient.bulkImport(bulkImport, transferFormat, compression);
          bulkImport.clear();
        }

        zis.closeEntry();
      }
    }

    if (!bulkImport.isEmpty()) {
      additionalAPIClient.bulkImport(bulkImport, transferFormat, compression);
    }
  }
}
