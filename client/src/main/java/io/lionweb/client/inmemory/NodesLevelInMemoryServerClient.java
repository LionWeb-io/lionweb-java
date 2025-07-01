package io.lionweb.client.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.*;
import io.lionweb.model.Node;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class NodesLevelInMemoryServerClient implements BulkAPIClient, DBAdminAPIClient {
  private final @NotNull InMemoryServer inMemoryServer;
  private @Nullable String repositoryName;

  public NodesLevelInMemoryServerClient(@NotNull InMemoryServer inMemoryServer) {
    this.inMemoryServer = inMemoryServer;
  }

  public NodesLevelInMemoryServerClient(
      @NotNull InMemoryServer inMemoryServer, @Nullable String repositoryName) {
    this.inMemoryServer = inMemoryServer;
    this.repositoryName = repositoryName;
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
  public RepositoryVersionToken createPartitions(List<Node> partitions) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> listPartitionsIDs() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Node> retrieve(List<String> nodeIds) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public RepositoryVersionToken deletePartitions(List<String> ids) {
    return inMemoryServer.deletePartitions(repositoryName, ids);
  }

  @Override
  public List<Node> listPartitions() {
    //return inMemoryServer.listPartitionIDs(repositoryName);
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> ids(int count) {
    return inMemoryServer.ids(repositoryName, count);
  }

  @Nullable
  @Override
  public RepositoryVersionToken store(List<Node> nodes) {
    // return inMemoryServer.store(repositoryName, nodes);
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Node> retrieve(List<String> nodeIds, int limit) {
    List<SerializedClassifierInstance> chunk = inMemoryServer.retrieve(repositoryName, nodeIds, limit);
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
