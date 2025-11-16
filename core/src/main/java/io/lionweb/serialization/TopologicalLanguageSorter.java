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
import javax.annotation.Nonnull;

/**
 * This knows how to sort a list of languages based on the same LionWeb Version according to their
 * dependencies, so that a language A depending on a language B is always specified after B.
 * Circular dependencies are not allowed and cause errors.
 */
public class TopologicalLanguageSorter {

  private final @Nonnull LionWebVersion lionWebVersion;

  public TopologicalLanguageSorter(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    this.lionWebVersion = lionWebVersion;
  }

  public @Nonnull List<SerializationChunk> topologicalSort(
      @Nonnull Collection<SerializationChunk> chunks) {
    List<SerializationChunk> result = new ArrayList<>();
    Set<String> visited = new HashSet<>();
    Set<String> visiting = new HashSet<>();

    Map<String, SerializationChunk> chunkMap = new HashMap<>();
    for (SerializationChunk chunk : chunks) {
      SerializedClassifierInstance languageNode = extractLanguageNode(chunk);
      if (languageNode != null) {
        String id = requireNonNull(languageNode.getID(), "languageNode.id is null");
        chunkMap.put(id, chunk);
      }
    }

    for (SerializationChunk chunk : chunks) {
      visit(chunk, visited, visiting, result, chunkMap);
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
      Map<String, SerializationChunk> chunkMap) {

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
        throw new IllegalStateException("Unknown dependency: " + depKey);
      }
      visit(dep, visited, visiting, out, chunkMap);
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
