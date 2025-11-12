package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.File;
import java.io.IOException;
import java.util.*;

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
            server.store(repositoryName, chunk.getClassifierInstances());
          }

          @Override
          public void partitionsLoaded() {}
        });
  }

  public static void store(File file, InMemoryServer server, String repositoryName)
      throws IOException {
    // Store metadata
    RepositoryConfiguration repositoryConfiguration =
        server.listRepositories().stream()
            .filter(r -> r.getName().equals(repositoryName))
            .findFirst()
            .get();
    LionWebVersion lionWebVersion = repositoryConfiguration.getLionWebVersion();
    LionWebArchive.store(
        file,
        lionWebVersion,
        Collections.<SerializationChunk>emptyList().stream(),
        server.listPartitionIDs(repositoryName).stream()
            .map(
                partitionId ->
                    SerializationChunk.fromNodes(
                        lionWebVersion,
                        server.retrieve(
                            repositoryName,
                            Collections.singletonList(partitionId),
                            Integer.MAX_VALUE))));
  }
}
