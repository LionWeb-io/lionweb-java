package io.lionweb.client.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.DBAdminAPIClient;
import io.lionweb.client.api.JSONLevelBulkAPIClient;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.data.SerializedChunk;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSONLevelInMemoryServerClient implements JSONLevelBulkAPIClient, DBAdminAPIClient {
  private @NotNull InMemoryServer inMemoryServer;
  private @Nullable String repositoryName;

  public JSONLevelInMemoryServerClient(@NotNull InMemoryServer inMemoryServer) {
    this.inMemoryServer = inMemoryServer;
  }

  public JSONLevelInMemoryServerClient(
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

  @Nullable
  @Override
  public RepositoryVersionToken rawCreatePartitions(@NotNull String data) {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public RepositoryVersionToken rawStore(@NotNull String nodes) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public String rawRetrieve(@Nullable List<String> nodeIds, int limit) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public String rawRetrieve(@Nullable List<String> nodeIds) {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public RepositoryVersionToken createPartitions(@NotNull SerializedChunk data) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public RepositoryVersionToken store(@NotNull SerializedChunk nodes) throws IOException {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public SerializedChunk retrieveAsChunk(@Nullable List<String> nodeIds, int limit)
      throws IOException {
    throw new UnsupportedOperationException();
  }
}
