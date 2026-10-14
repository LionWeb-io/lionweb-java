package io.lionweb.serialization;

import static java.util.Objects.requireNonNull;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Concept;
import io.lionweb.lioncore.LionCore;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedReferenceValue;
import java.util.*;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

/**
 * This knows how to sort a list of languages based on the same LionWeb Version according to their
 * dependencies, so that a language A depending on a language B is always specified after B.
 * Circular dependencies are not allowed and cause errors.
 *
 * <p>By default, every dependency must be one of the languages being sorted, otherwise an error is
 * raised. Dependencies on languages known by other means (e.g., already registered in a
 * serialization) can be accepted by using {@link #topologicalSort(Collection, Predicate)}.
 */
public class TopologicalLanguageSorter {

  private final @Nonnull LionWebVersion lionWebVersion;

  public TopologicalLanguageSorter(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    this.lionWebVersion = lionWebVersion;
  }

  /**
   * Sorts the given language chunks so that each language follows the languages it depends on.
   *
   * @throws IllegalStateException if a language depends on a language not among the given chunks,
   *     if there are cyclic dependencies, or if there are duplicate languages
   */
  public @Nonnull List<SerializationChunk> topologicalSort(
      @Nonnull Collection<SerializationChunk> chunks) {
    return topologicalSort(chunks, languageID -> false);
  }

  /**
   * Sorts the given language chunks so that each language follows the languages it depends on.
   *
   * @param chunks the language chunks to sort
   * @param isKnownDependency tells if a dependency which is not among the given chunks should be
   *     accepted (and ignored for the purpose of sorting). It receives the ID of the language
   *     depended upon. Use {@code id -> true} to ignore all unknown dependencies.
   * @throws IllegalStateException if a language depends on a language not among the given chunks
   *     and not accepted by {@code isKnownDependency}, if there are cyclic dependencies, or if
   *     there are duplicate languages
   */
  public @Nonnull List<SerializationChunk> topologicalSort(
      @Nonnull Collection<SerializationChunk> chunks,
      @Nonnull Predicate<String> isKnownDependency) {
    Objects.requireNonNull(chunks, "chunks should not be null");
    Objects.requireNonNull(isKnownDependency, "isKnownDependency should not be null");
    List<SerializationChunk> result = new ArrayList<>();
    Set<String> visited = new HashSet<>();
    Set<String> visiting = new HashSet<>();

    Map<String, SerializationChunk> chunkMap = new HashMap<>();
    for (SerializationChunk chunk : chunks) {
      SerializedClassifierInstance languageNode = extractLanguageNode(chunk);
      if (languageNode != null) {
        String id = requireNonNull(languageNode.getID(), "languageNode.id is null");
        if (chunkMap.containsKey(id)) {
          throw new IllegalStateException("Duplicate language ID found: " + id);
        }
        chunkMap.put(id, chunk);
      }
    }

    for (SerializationChunk chunk : chunks) {
      visit(chunk, visited, visiting, result, chunkMap, isKnownDependency);
    }

    return result;
  }

  private Set<String> deps(SerializedClassifierInstance languageNode) {
    Concept languageConcept = LionCore.getLanguage(lionWebVersion);
    MetaPointer dependsOnPtr = MetaPointer.from(languageConcept.getReferenceByName("dependsOn"));
    List<SerializedReferenceValue.Entry> usedLanguages =
        languageNode.getReferenceValues(dependsOnPtr);

    Set<String> result = new LinkedHashSet<>();
    for (SerializedReferenceValue.Entry rv : usedLanguages) {
      result.add(String.valueOf(rv.getReference()));
    }
    return result;
  }

  private void visit(
      SerializationChunk chunk,
      Set<String> visited,
      Set<String> visiting,
      List<SerializationChunk> out,
      Map<String, SerializationChunk> chunkMap,
      Predicate<String> isKnownDependency) {

    SerializedClassifierInstance languageNode = extractLanguageNode(chunk);
    if (languageNode == null) return;
    String key = requireNonNull(languageNode.getID(), "languageNode.id is null");

    if (visited.contains(key)) return;
    if (visiting.contains(key)) {
      throw new IllegalStateException("Cyclic dependency detected at " + key);
    }

    visiting.add(key);
    for (String depKey : deps(languageNode)) {
      SerializationChunk dep = chunkMap.get(depKey);
      if (dep == null) {
        if (isKnownDependency.test(depKey)) {
          continue;
        }
        throw new IllegalStateException("Unknown dependency: " + depKey);
      }
      visit(dep, visited, visiting, out, chunkMap, isKnownDependency);
    }
    visiting.remove(key);
    visited.add(key);
    out.add(chunk);
  }

  /** Extract the single "language" node from a chunk. */
  private SerializedClassifierInstance extractLanguageNode(SerializationChunk chunk) {
    MetaPointer languageClassifier = MetaPointer.from(LionCore.getLanguage(lionWebVersion));

    List<SerializedClassifierInstance> matches = new ArrayList<>();
    for (SerializedClassifierInstance sci : chunk.getClassifierInstances()) {
      if (languageClassifier.equals(sci.getClassifier())) {
        matches.add(sci);
      }
    }
    if (matches.isEmpty()) {
      return null;
    }
    if (matches.size() != 1) {
      throw new IllegalStateException(
          "Expected exactly one language node, found " + matches.size());
    }
    return matches.get(0);
  }
}
