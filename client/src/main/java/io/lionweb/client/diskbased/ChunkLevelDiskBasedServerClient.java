package io.lionweb.client.diskbased;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.*;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This Client for the InMemoryServer operates on SerializedClassifierInstances. This has several
 * benefits: - This is the format used by the InMemoryServer, so it is the most efficient - One can
 * work without knowing the languages
 */
public class ChunkLevelDiskBasedServerClient
    implements ChunkLevelBulkAPIClient, DBAdminAPIClient, InspectionAPIClient {
  private final @NotNull DiskBasedServer diskBasedServer;
  private @Nullable String repositoryName;

  /**
   * The repositoryName is not set, so operations like creating a repository or getting the list of
   * repositories can still be performed, while other operations will require setting the
   * repositoryName first.
   */
  public ChunkLevelDiskBasedServerClient(@NotNull DiskBasedServer diskBasedServer) {
    this.diskBasedServer = diskBasedServer;
  }

  public ChunkLevelDiskBasedServerClient(
      @NotNull DiskBasedServer diskBasedServer, @Nullable String repositoryName) {
    this.diskBasedServer = diskBasedServer;
    this.repositoryName = repositoryName;
  }

  @Override
  public @NotNull List<String> ids(int count) {
    requireRepository();
    return diskBasedServer.ids(repositoryName, count);
  }

  @Override
  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
    Objects.requireNonNull(repositoryConfiguration);
    diskBasedServer.createRepository(repositoryConfiguration);
  }

  @Override
  public void deleteRepository(@NotNull String repositoryName) {
    diskBasedServer.deleteRepository(repositoryName);
  }

  @NotNull
  @Override
  public Set<RepositoryConfiguration> listRepositories() {
    return diskBasedServer.listRepositories();
  }

  @Override
  public @NotNull List<String> listPartitionsIDs() {
    requireRepository();
    return diskBasedServer.listPartitionIDs(repositoryName);
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
    return diskBasedServer.getRepositoryConfiguration(repositoryName).getLionWebVersion();
  }

  @Nullable
  @Override
  public RepositoryVersionToken createPartitionsFromChunk(
      @NotNull List<SerializedClassifierInstance> data) {
    requireRepository();
    return diskBasedServer.createPartitionFromChunk(repositoryName, data);
  }

  @Nullable
  @Override
  public RepositoryVersionToken deletePartitions(List<String> ids) {
    requireRepository();
    return diskBasedServer.deletePartitions(repositoryName, ids);
  }

  @Nullable
  @Override
  public RepositoryVersionToken storeChunk(@NotNull List<SerializedClassifierInstance> nodes) {
    requireRepository();
    return diskBasedServer.store(repositoryName, nodes);
  }

  @NotNull
  @Override
  public List<SerializedClassifierInstance> retrieveAsChunk(
      @Nullable List<String> nodeIds, int limit) {
    requireRepository();
    return diskBasedServer.retrieve(repositoryName, nodeIds, limit);
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
    return diskBasedServer.nodesByClassifier(repositoryName);
  }

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(@Nullable Integer limit) {
    requireRepository();
    return diskBasedServer.nodesByClassifier(repositoryName, limit);
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage() {
    requireRepository();
    return diskBasedServer.nodesByLanguage(repositoryName);
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage(@Nullable Integer limit) {
    requireRepository();
    return diskBasedServer.nodesByLanguage(repositoryName, limit);
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
