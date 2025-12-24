package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.lioncore.LionCore;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.Nonnull;

/**
 * The RepositoryStorage class provides functionalities to load and store repositories using the
 * LionWebArchive format. It interacts with an in-memory server and handles serialization and
 * deserialization of repository data, including language definitions and partition chunks.
 */
public class RepositoryStorage {

  /**
   * Loads the content of a given file into a specified repository on an in-memory server. This
   * method optionally stores language definitions from the file.
   *
   * @param file the file to be loaded
   * @param server the in-memory server where the repository resides
   * @param repositoryName the name of the repository where the content will be loaded
   * @param storeAlsoLanguages a flag indicating whether language definitions should also be stored
   * @throws IOException if an I/O error occurs during loading
   */
  public static void load(
      @Nonnull File file,
      @Nonnull InMemoryServer server,
      @Nonnull String repositoryName,
      boolean storeAlsoLanguages)
      throws IOException {

    Objects.requireNonNull(file, "file should not be null");
    Objects.requireNonNull(server, "server should not be null");
    Objects.requireNonNull(repositoryName, "repositoryName should not be null");
    LionWebArchive.load(
        file,
        new LionWebArchive.Loader() {

          @Override
          public void setLwVersion(LionWebVersion lionWebVersion) {
            if (server.listRepositories().stream()
                .noneMatch(r -> r.getName().equals(repositoryName))) {
              server.createRepository(
                  new RepositoryConfiguration(
                      repositoryName, lionWebVersion, HistorySupport.DISABLED));
            }
          }

          public void addLanguageChunk(SerializationChunk chunk) {
            if (storeAlsoLanguages) {
              server.createPartitionFromChunk(repositoryName, chunk.getClassifierInstances());
            }
          }

          @Override
          public void languagesLoaded() {}

          @Override
          public void addPartitionChunk(SerializationChunk chunk) {
            server.createPartitionFromChunk(repositoryName, chunk.getClassifierInstances());
          }

          @Override
          public void partitionsLoaded() {}
        });
  }

  /**
   * Stores the content of a given file into a specified repository on an in-memory server. This
   * method provides a simplified interface that assumes language definitions should also be stored.
   *
   * @param file the file whose content is to be stored
   * @param server the in-memory server where the repository resides
   * @param repositoryName the name of the repository where the content will be stored
   * @throws IOException if an I/O error occurs during the storage process
   */
  public static void store(
      @Nonnull File file, @Nonnull InMemoryServer server, @Nonnull String repositoryName)
      throws IOException {
    store(file, server, repositoryName, true);
  }

  /**
   * Stores the content of a repository into a file, including its partitions and optionally
   * language definitions, based on the specified parameters. The content is serialized into a file
   * in the LionWeb Archive format.
   *
   * @param file the file where the repository's content will be stored; must not be null
   * @param server the in-memory server containing the repository; must not be null
   * @param repositoryName the name of the repository whose content will be stored; must not be null
   * @param languagesOutOfBound a flag indicating whether language definitions are stored separately
   *     in the "languages out-of-bound" queue
   * @throws IOException if an I/O error occurs during the storage process
   */
  public static void store(
      @Nonnull File file,
      @Nonnull InMemoryServer server,
      @Nonnull String repositoryName,
      boolean languagesOutOfBound)
      throws IOException {
    Objects.requireNonNull(file, "file should not be null");
    Objects.requireNonNull(server, "server should not be null");
    Objects.requireNonNull(repositoryName, "repositoryName should not be null");
    // Store metadata
    RepositoryConfiguration repositoryConfiguration =
        server.listRepositories().stream()
            .filter(r -> r.getName().equals(repositoryName))
            .findFirst()
            .get();
    LionWebVersion lionWebVersion = repositoryConfiguration.getLionWebVersion();
    SerializationChunk END_MARKER = new SerializationChunk();
    final BlockingQueue<SerializationChunk> queueLanguages = new LinkedBlockingQueue<>();
    final BlockingQueue<SerializationChunk> queuePartitions = new LinkedBlockingQueue<>();
    Iterable<SerializationChunk> iterableLanguages =
        () ->
            new Iterator<SerializationChunk>() {
              SerializationChunk next;

              public boolean hasNext() {
                try {
                  next = queueLanguages.take();
                } catch (InterruptedException e) {
                  return false;
                }
                return next != END_MARKER;
              }

              public SerializationChunk next() {
                if (next == END_MARKER) throw new NoSuchElementException();
                return next;
              }
            };
    Iterable<SerializationChunk> iterablePartitions =
        () ->
            new Iterator<SerializationChunk>() {
              SerializationChunk next;

              public boolean hasNext() {
                try {
                  next = queuePartitions.take();
                } catch (InterruptedException e) {
                  return false;
                }
                return next != END_MARKER;
              }

              public SerializationChunk next() {
                return next;
              }
            };
    server.listPartitionIDs(repositoryName).stream()
        .forEach(
            partitionId -> {
              List<SerializedClassifierInstance> serializedNodes =
                  server.retrieve(
                      repositoryName, Collections.singletonList(partitionId), Integer.MAX_VALUE);
              boolean outOfBound = false;
              if (languagesOutOfBound) {
                SerializedClassifierInstance root =
                    serializedNodes.stream()
                        .filter(n -> n.getParentNodeID() == null)
                        .findFirst()
                        .get();
                if (root.getClassifier()
                    .equals(MetaPointer.from(LionCore.getLanguage(lionWebVersion)))) {
                  outOfBound = true;
                }
              }
              if (outOfBound) {
                queueLanguages.add(SerializationChunk.fromNodes(lionWebVersion, serializedNodes));
              } else {
                queuePartitions.add(SerializationChunk.fromNodes(lionWebVersion, serializedNodes));
              }
            });
    queueLanguages.add(END_MARKER);
    queuePartitions.add(END_MARKER);

    LionWebArchive.store(file, lionWebVersion, iterableLanguages, iterablePartitions);
  }
}
