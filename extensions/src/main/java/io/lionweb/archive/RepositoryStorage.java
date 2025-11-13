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

public class RepositoryStorage {

  public static void load(
      File file, InMemoryServer server, String repositoryName, boolean storeAlsoLanguages)
      throws IOException {

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
              server.store(repositoryName, chunk.getClassifierInstances());
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

  public static void store(File file, InMemoryServer server, String repositoryName)
      throws IOException {
    store(file, server, repositoryName, true);
  }

  public static void store(
      File file, InMemoryServer server, String repositoryName, boolean languagesOutOfBound)
      throws IOException {
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
