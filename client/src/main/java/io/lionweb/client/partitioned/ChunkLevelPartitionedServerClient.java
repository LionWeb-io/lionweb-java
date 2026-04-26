package io.lionweb.client.partitioned;

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
 * A client for {@link PartitionedServer} that operates on {@link SerializedClassifierInstance}
 * objects (chunk level).
 *
 * <p>This is the partition-oriented equivalent of {@code ChunkLevelInMemoryServerClient} and
 * implements the same interfaces with the same method signatures.
 */
public class ChunkLevelPartitionedServerClient
    implements ChunkLevelBulkAPIClient, DBAdminAPIClient, InspectionAPIClient {

  private final @NotNull PartitionedServer partitionedServer;
  private @Nullable String repositoryName;

  /**
   * Creates a client without a default repository. Call {@link #setRepositoryName(String)} before
   * invoking repository-scoped operations.
   */
  public ChunkLevelPartitionedServerClient(@NotNull PartitionedServer partitionedServer) {
    this.partitionedServer = partitionedServer;
  }

  public ChunkLevelPartitionedServerClient(
      @NotNull PartitionedServer partitionedServer, @Nullable String repositoryName) {
    this.partitionedServer = partitionedServer;
    this.repositoryName = repositoryName;
  }

  @Nullable
  public String getRepositoryName() {
    return repositoryName;
  }

  public void setRepositoryName(@Nullable String repositoryName) {
    this.repositoryName = repositoryName;
  }

  // -------------------------------------------------------------------------
  // DBAdminAPIClient
  // -------------------------------------------------------------------------

  @Override
  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
    Objects.requireNonNull(repositoryConfiguration);
    partitionedServer.createRepository(repositoryConfiguration);
  }

  @Override
  public void deleteRepository(@NotNull String repositoryName) {
    partitionedServer.deleteRepository(repositoryName);
  }

  @Override
  public void createDatabase() {
    // No-op for embedded server
  }

  @NotNull
  @Override
  public Set<RepositoryConfiguration> listRepositories() {
    return partitionedServer.listRepositories();
  }

  // -------------------------------------------------------------------------
  // ChunkLevelBulkAPIClient
  // -------------------------------------------------------------------------

  @NotNull
  @Override
  public LionWebVersion getLionWebVersion() {
    requireRepository();
    return partitionedServer.getRepositoryConfiguration(repositoryName).getLionWebVersion();
  }

  @NotNull
  @Override
  public List<String> ids(int count) {
    requireRepository();
    return partitionedServer.ids(repositoryName, count);
  }

  @NotNull
  @Override
  public List<String> listPartitionsIDs() {
    requireRepository();
    return partitionedServer.listPartitionIDs(repositoryName);
  }

  @Nullable
  @Override
  public RepositoryVersionToken createPartitionsFromChunk(
      @NotNull List<SerializedClassifierInstance> data) {
    requireRepository();
    return partitionedServer.createPartitionFromChunk(repositoryName, data);
  }

  @Nullable
  @Override
  public RepositoryVersionToken deletePartitions(List<String> ids) {
    requireRepository();
    return partitionedServer.deletePartitions(repositoryName, ids);
  }

  @Nullable
  @Override
  public RepositoryVersionToken storeChunk(@NotNull List<SerializedClassifierInstance> nodes) {
    requireRepository();
    return partitionedServer.store(repositoryName, nodes);
  }

  @NotNull
  @Override
  public List<SerializedClassifierInstance> retrieveAsChunk(
      @Nullable List<String> nodeIds, int limit) {
    requireRepository();
    return partitionedServer.retrieve(repositoryName, nodeIds, limit);
  }

  // -------------------------------------------------------------------------
  // InspectionAPIClient
  // -------------------------------------------------------------------------

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(@Nullable Integer limit) {
    requireRepository();
    return partitionedServer.nodesByClassifier(repositoryName, limit);
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage(@Nullable Integer limit) {
    requireRepository();
    return partitionedServer.nodesByLanguage(repositoryName, limit);
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private void requireRepository() {
    if (repositoryName == null) {
      throw new IllegalStateException("This API requires the repositoryName to be set");
    }
  }
}
