package io.lionweb.client.inmemory;

import io.lionweb.client.api.*;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.utils.ValidationResult;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An InMemoryServer is useful for testing and as a replacement for a proper server, when performing
 * processing operations.
 *
 * <p>We store data using SerializedClassifierInstance so that: - We do not need to know the
 * languages - We can inspect the nodes while we could not if we stored the data serialized in JSON
 * or binary formats.
 *
 * <p>Different clients can then still work with nodes or JSON or binary formats.
 */
public class InMemoryServer {

  /** Internally we store the data separately for each repository. */
  private final Map<String, RepositoryData> repositories = new LinkedHashMap<>();

  public @NotNull RepositoryConfiguration getRepositoryConfiguration(
      @NotNull String repositoryName) {
    return getRepository(repositoryName).configuration;
  }

  public @NotNull List<String> ids(@NotNull String repositoryName, int count) {
    if (count < 0) {
      throw new IllegalArgumentException("One can ask for zero or more ids");
    }
    RepositoryData repositoryData = getRepository(repositoryName);
    return repositoryData.ids(count);
  }

  public @NotNull Set<RepositoryConfiguration> listRepositories() {
    return repositories.values().stream().map(r -> r.configuration).collect(Collectors.toSet());
  }

  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) {
    Objects.requireNonNull(repositoryConfiguration);
    if (repositoryConfiguration.getHistorySupport() == HistorySupport.ENABLED) {
      throw new IllegalArgumentException(
          "The InMemoryServer does not support History for the time being");
    }
    repositories.put(
        repositoryConfiguration.getName(), new RepositoryData(repositoryConfiguration));
  }

  public void deleteRepository(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName);
    if (!repositories.containsKey(repositoryName)) {
      throw new IllegalArgumentException();
    }
    repositories.remove(repositoryName);
  }

  public @NotNull List<String> listPartitionIDs(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    return repositoryData.partitionIDs;
  }

  public @NotNull RepositoryVersionToken createPartitionFromChunk(
      @NotNull String repositoryName, @NotNull List<SerializedClassifierInstance> partitions) {
    Objects.requireNonNull(partitions);
    RepositoryData repositoryData = getRepository(repositoryName);
    // We get all roots (i.e. -> partitions) which do not yet exist
    // and add them to the list of partition IDs
    repositoryData.partitionIDs.addAll(
        partitions.stream()
            .filter(n -> n.getParentNodeID() == null)
            .map(SerializedClassifierInstance::getID)
            .filter(id -> !repositoryData.partitionIDs.contains(id))
            .collect(Collectors.toList()));
    repositoryData.store(partitions);
    return repositoryData.bumpVersion();
  }

  public @NotNull RepositoryVersionToken deletePartitions(
      @NotNull String repositoryName, @NotNull List<String> partitionIds) {
    Objects.requireNonNull(partitionIds);
    RepositoryData repositoryData = getRepository(repositoryName);
    repositoryData.partitionIDs.removeIf(partitionIds::contains);
    partitionIds.forEach(repositoryData::deleteNodeAndDescendant);
    return repositoryData.bumpVersion();
  }

  public List<SerializedClassifierInstance> retrieve(
      @NotNull String repositoryName, List<String> nodeIds, int limit) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    List<SerializedClassifierInstance> retrieved = new ArrayList<>();
    nodeIds.forEach(n -> repositoryData.retrieve(n, limit, retrieved));
    return retrieved;
  }

  public RepositoryVersionToken store(
      @NotNull String repositoryName, @NotNull List<SerializedClassifierInstance> nodes) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    repositoryData.store(nodes);
    return repositoryData.bumpVersion();
  }

  //
  // Inspection
  //

  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(@NotNull String repositoryName) {
    return nodesByClassifier(repositoryName, Integer.MAX_VALUE);
  }

  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(
      @NotNull String repositoryName, @Nullable Integer limit) {
    RepositoryData repositoryData = getRepository(repositoryName);
    Map<MetaPointer, List<SerializedClassifierInstance>> byMetapointer =
        repositoryData.nodesByID.values().stream()
            .collect(Collectors.groupingBy(n -> n.getClassifier()));
    Map<ClassifierKey, ClassifierResult> res = new HashMap<>();
    for (Map.Entry<MetaPointer, List<SerializedClassifierInstance>> entry :
        byMetapointer.entrySet()) {
      ClassifierKey key = new ClassifierKey(entry.getKey().getLanguage(), entry.getKey().getKey());
      ClassifierResult cr =
          new ClassifierResult(
              entry.getValue().stream()
                  .limit(limit)
                  .map(n -> n.getID())
                  .collect(Collectors.toSet()),
              entry.getValue().size());
      res.put(key, cr);
    }
    return res;
  }

  public Map<String, ClassifierResult> nodesByLanguage(@NotNull String repositoryName) {
    return nodesByLanguage(repositoryName, Integer.MAX_VALUE);
  }

  public Map<String, ClassifierResult> nodesByLanguage(
      @NotNull String repositoryName, @Nullable Integer limit) {
    RepositoryData repositoryData = getRepository(repositoryName);
    Map<String, List<SerializedClassifierInstance>> byMetapointer =
        repositoryData.nodesByID.values().stream()
            .collect(Collectors.groupingBy(n -> n.getClassifier().getLanguage()));
    Map<String, ClassifierResult> res = new HashMap<>();
    for (Map.Entry<String, List<SerializedClassifierInstance>> entry : byMetapointer.entrySet()) {
      ClassifierResult cr =
          new ClassifierResult(
              entry.getValue().stream()
                  .limit(limit)
                  .map(n -> n.getID())
                  .collect(Collectors.toSet()),
              entry.getValue().size());
      res.put(entry.getKey(), cr);
    }
    return res;
  }

  /**
   * Checks the consistency of all repositories stored in the system and aggregates any validation
   * issues found into a single {@link ValidationResult}.
   *
   * <p>The method iterates through all repository data, invokes their individual consistency
   * checks, and collects any issues reported into the resulting validation result object.
   *
   * <p>This is intended for debugging purposes.
   *
   * @return a {@link ValidationResult} containing all identified issues, or an empty result if no
   *     issues were found.
   */
  public @NotNull ValidationResult checkConsistency() {
    ValidationResult result = new ValidationResult();
    for (RepositoryData repositoryData : repositories.values()) {
      ValidationResult partial = repositoryData.checkConsistency();
      result.getIssues().addAll(partial.getIssues());
    }
    return result;
  }

  //
  // Private methods
  //

  private @NotNull RepositoryData getRepository(@NotNull String repositoryName) {
    Objects.requireNonNull(repositoryName, "RepositoryName should not be null");
    RepositoryData repositoryData = repositories.get(repositoryName);
    if (repositoryData == null) {
      throw new IllegalArgumentException("Cannot find repository named " + repositoryName);
    }
    return repositoryData;
  }
}
