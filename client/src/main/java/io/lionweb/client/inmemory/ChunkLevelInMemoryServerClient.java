package io.lionweb.client.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.*;
import io.lionweb.model.Node;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.lionweb.serialization.data.SerializedChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChunkLevelInMemoryServerClient implements ChunkLevelBulkAPIClient, DBAdminAPIClient {
  private final @NotNull InMemoryServer inMemoryServer;
  private @Nullable String repositoryName;

  public ChunkLevelInMemoryServerClient(@NotNull InMemoryServer inMemoryServer) {
    this.inMemoryServer = inMemoryServer;
  }

  public ChunkLevelInMemoryServerClient(
      @NotNull InMemoryServer inMemoryServer, @Nullable String repositoryName) {
    this.inMemoryServer = inMemoryServer;
    this.repositoryName = repositoryName;
  }

  @Override public @NotNull List<String> listPartitionsIDs() {
    return inMemoryServer.listPartitionIDs(repositoryName);
  }

  @Nullable
  public String getRepositoryName() {
    return repositoryName;
  }

  public void setRepositoryName(@Nullable String repositoryName) {
    this.repositoryName = repositoryName;
  }

  @NotNull
  @Override
  public LionWebVersion getLionWebVersion() {
    return inMemoryServer.getRepositoryConfiguration(repositoryName).getLionWebVersion();
  }

  @Nullable
  @Override
  public RepositoryVersionToken createPartitionsFromChunk(@NotNull List<SerializedClassifierInstance> data) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public RepositoryVersionToken deletePartitions(List<String> ids) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public RepositoryVersionToken storeChunk(@NotNull List<SerializedClassifierInstance> nodes) throws IOException {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public List<SerializedClassifierInstance> retrieveAsChunk(@Nullable List<String> nodeIds, int limit) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
    Objects.requireNonNull(repositoryConfiguration);
    inMemoryServer.createRepository(repositoryConfiguration);
  }

  @Override
  public void deleteRepository(@NotNull String repositoryName) {
    inMemoryServer.deleteRepository(repositoryName);
  }

  @Override
  public void createDatabase() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public Set<RepositoryConfiguration> listRepositories() {
    return inMemoryServer.listRepositories();
  }
}
