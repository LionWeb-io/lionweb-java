package io.lionweb.client.partitioned;

import io.lionweb.serialization.ProtoBufSerialization;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Disk-based {@link RepositoryBackend} that stores each partition as a ProtoBuf file.
 *
 * <h3>File layout</h3>
 *
 * <pre>
 *   &lt;storageDir&gt;/
 *     &lt;repositoryName&gt;/
 *       &lt;partitionId&gt;.pb
 * </pre>
 *
 * <p>LionWeb IDs only contain {@code [a-zA-Z0-9_-]}, so partition IDs are safe to use directly as
 * file names.
 *
 * <h3>Format</h3>
 *
 * <p>Each file is a standard LionWeb ProtoBuf chunk as produced by {@link ProtoBufSerialization}.
 * The file is self-describing: the LionWeb version is embedded in the chunk header.
 *
 * <h3>Write strategy</h3>
 *
 * <p>Each save writes the full partition to a temporary file in the same directory, then atomically
 * renames it over the target file. This prevents corrupt reads if the JVM crashes mid-write.
 */
public final class DiskRepositoryBackend implements RepositoryBackend {

  private static final String EXTENSION = ".pb";

  private final Path storageDir;
  private final ProtoBufSerialization proto = new ProtoBufSerialization();

  public DiskRepositoryBackend(Path storageDir) {
    this.storageDir = storageDir;
  }

  @Override
  public List<String> listPersistedPartitionIds(String repositoryName) throws IOException {
    Path repoDir = repoDir(repositoryName);
    if (!Files.isDirectory(repoDir)) {
      return Collections.emptyList();
    }
    try (Stream<Path> files = Files.list(repoDir)) {
      return files
          .filter(p -> p.getFileName().toString().endsWith(EXTENSION))
          .map(
              p -> {
                String name = p.getFileName().toString();
                return name.substring(0, name.length() - EXTENSION.length());
              })
          .collect(Collectors.toList());
    }
  }

  @Override
  public List<SerializedClassifierInstance> loadPartition(
      String repositoryName, String partitionId) throws IOException {
    Path file = partitionFile(repositoryName, partitionId);
    if (!Files.exists(file)) {
      return Collections.emptyList();
    }
    byte[] bytes = Files.readAllBytes(file);
    SerializationChunk chunk = proto.deserializeToChunk(bytes);
    return new ArrayList<>(chunk.getClassifierInstances());
  }

  @Override
  public void savePartition(
      String repositoryName, String partitionId, SerializationChunk chunk) throws IOException {
    Path repoDir = repoDir(repositoryName);
    Files.createDirectories(repoDir);
    byte[] bytes = proto.serializeToByteArray(chunk);

    // Write to temp file then atomically rename to avoid partial reads
    Path target = repoDir.resolve(partitionId + EXTENSION);
    Path tmp = repoDir.resolve(partitionId + ".pb.tmp");
    Files.write(tmp, bytes);
    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
  }

  @Override
  public void deletePartition(String repositoryName, String partitionId) throws IOException {
    Path file = partitionFile(repositoryName, partitionId);
    Files.deleteIfExists(file);
  }

  @Override
  public boolean hasPartition(String repositoryName, String partitionId) throws IOException {
    return Files.exists(partitionFile(repositoryName, partitionId));
  }

  @Override
  public void deleteRepository(String repositoryName) throws IOException {
    Path repoDir = repoDir(repositoryName);
    if (!Files.isDirectory(repoDir)) {
      return;
    }
    try (Stream<Path> files = Files.list(repoDir)) {
      for (Path f : files.collect(Collectors.toList())) {
        Files.deleteIfExists(f);
      }
    }
    Files.deleteIfExists(repoDir);
  }

  @Override
  public void close() throws IOException {
    // No resources to release for a file-based backend
  }

  public Path getStorageDir() {
    return storageDir;
  }

  private Path repoDir(String repositoryName) {
    return storageDir.resolve(repositoryName);
  }

  private Path partitionFile(String repositoryName, String partitionId) {
    return repoDir(repositoryName).resolve(partitionId + EXTENSION);
  }
}
