package io.lionweb.client.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.*;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Client for {@link DiskBackedServer} that operates on {@link SerializedClassifierInstance}
 * objects directly, without requiring language definitions.
 *
 * <p>This is the disk-backed counterpart of {@link ChunkLevelInMemoryServerClient} and has an
 * identical API.
 */
public class ChunkLevelDiskBackedServerClient
    implements ChunkLevelBulkAPIClient, DBAdminAPIClient, InspectionAPIClient {

  private final @NotNull DiskBackedServer server;
  private @Nullable String repositoryName;

  public ChunkLevelDiskBackedServerClient(@NotNull DiskBackedServer server) {
    this.server = server;
  }

  public ChunkLevelDiskBackedServerClient(
      @NotNull DiskBackedServer server, @Nullable String repositoryName) {
    this.server = server;
    this.repositoryName = repositoryName;
  }

  @Nullable
  public String getRepositoryName() {
    return repositoryName;
  }

  public void setRepositoryName(@Nullable String repositoryName) {
    this.repositoryName = repositoryName;
  }

  @Override
  public @NotNull LionWebVersion getLionWebVersion() {
    requireRepository();
    return server.getRepositoryConfiguration(repositoryName).getLionWebVersion();
  }

  @Override
  public @NotNull List<String> ids(int count) {
    requireRepository();
    return server.ids(repositoryName, count);
  }

  @Override
  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
    Objects.requireNonNull(repositoryConfiguration);
    server.createRepository(repositoryConfiguration);
  }

  @Override
  public void deleteRepository(@NotNull String repositoryName) {
    server.deleteRepository(repositoryName);
  }

  @NotNull
  @Override
  public Set<RepositoryConfiguration> listRepositories() {
    return server.listRepositories();
  }

  @Override
  public @NotNull List<String> listPartitionsIDs() {
    requireRepository();
    return server.listPartitionIDs(repositoryName);
  }

  @Nullable
  @Override
  public RepositoryVersionToken createPartitionsFromChunk(
      @NotNull List<SerializedClassifierInstance> data) {
    requireRepository();
    return server.createPartitionFromChunk(repositoryName, data);
  }

  @Nullable
  @Override
  public RepositoryVersionToken deletePartitions(List<String> ids) {
    requireRepository();
    return server.deletePartitions(repositoryName, ids);
  }

  @Nullable
  @Override
  public RepositoryVersionToken storeChunk(@NotNull List<SerializedClassifierInstance> nodes) {
    requireRepository();
    return server.store(repositoryName, nodes);
  }

  @NotNull
  @Override
  public List<SerializedClassifierInstance> retrieveAsChunk(
      @Nullable List<String> nodeIds, int limit) {
    requireRepository();
    return server.retrieve(repositoryName, nodeIds, limit);
  }

  @Override
  public void createDatabase() {
    // Nothing to do
  }

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier() {
    requireRepository();
    return server.nodesByClassifier(repositoryName);
  }

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(@Nullable Integer limit) {
    requireRepository();
    return server.nodesByClassifier(repositoryName, limit);
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage() {
    requireRepository();
    return server.nodesByLanguage(repositoryName);
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage(@Nullable Integer limit) {
    requireRepository();
    return server.nodesByLanguage(repositoryName, limit);
  }

  private void requireRepository() {
    if (repositoryName == null) {
      throw new IllegalStateException("This API requires the repositoryName to be set");
    }
  }
}
