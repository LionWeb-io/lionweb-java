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
 * This Client for the InMemoryServer operates on SerializedClassifierInstances. This has several
 * benefits: - This is the format used by the InMemoryServer, so it is the most efficient - One can
 * work without knowing the languages
 */
public class ChunkLevelInMemoryServerClient
    implements ChunkLevelBulkAPIClient, DBAdminAPIClient, InspectionAPIClient {
  private final @NotNull InMemoryServer inMemoryServer;
  private @Nullable String repositoryName;

  /**
   * The repositoryName is not set, so operations like creating a repository or getting the list of
   * repositories can still be performed, while other operations will require setting the
   * repositoryName first.
   */
  public ChunkLevelInMemoryServerClient(@NotNull InMemoryServer inMemoryServer) {
    this.inMemoryServer = inMemoryServer;
  }

  public ChunkLevelInMemoryServerClient(
      @NotNull InMemoryServer inMemoryServer, @Nullable String repositoryName) {
    this.inMemoryServer = inMemoryServer;
    this.repositoryName = repositoryName;
  }

  @Override
  public @NotNull List<String> ids(int count) {
    requireRepository();
    return inMemoryServer.ids(repositoryName, count);
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

  @NotNull
  @Override
  public Set<RepositoryConfiguration> listRepositories() {
    return inMemoryServer.listRepositories();
  }

  @Override
  public @NotNull List<String> listPartitionsIDs() {
    requireRepository();
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
    requireRepository();
    return inMemoryServer.getRepositoryConfiguration(repositoryName).getLionWebVersion();
  }

  @Nullable
  @Override
  public RepositoryVersionToken createPartitionsFromChunk(
      @NotNull List<SerializedClassifierInstance> data) {
    requireRepository();
    return inMemoryServer.createPartitionFromChunk(repositoryName, data);
  }

  @Nullable
  @Override
  public RepositoryVersionToken deletePartitions(List<String> ids) {
    requireRepository();
    return inMemoryServer.deletePartitions(repositoryName, ids);
  }

  @Nullable
  @Override
  public RepositoryVersionToken storeChunk(@NotNull List<SerializedClassifierInstance> nodes) {
    requireRepository();
    return inMemoryServer.store(repositoryName, nodes);
  }

  @NotNull
  @Override
  public List<SerializedClassifierInstance> retrieveAsChunk(
      @Nullable List<String> nodeIds, int limit) {
    requireRepository();
    return inMemoryServer.retrieve(repositoryName, nodeIds, limit);
  }

  @Override
  public void createDatabase() {
    // Nothing to do
  }

  //
  // InspectionAPIClient methods
  //

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier() {
    requireRepository();
    return inMemoryServer.nodesByClassifier(repositoryName);
  }

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(@Nullable Integer limit) {
    requireRepository();
    return inMemoryServer.nodesByClassifier(repositoryName, limit);
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage() {
    requireRepository();
    return inMemoryServer.nodesByLanguage(repositoryName);
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage(@Nullable Integer limit) {
    requireRepository();
    return inMemoryServer.nodesByLanguage(repositoryName, limit);
  }

  //
  // Private methods
  //

  private void requireRepository() {
    if (repositoryName == null) {
      throw new IllegalStateException("This API requires the repositoryName to be set");
    }
  }
}
