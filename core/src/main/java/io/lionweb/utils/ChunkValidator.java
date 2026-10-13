package io.lionweb.utils;

import io.lionweb.LionWebVersion;
import io.lionweb.serialization.data.*;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Validate a generic chunk, which may contain part of a partition, an entire partition, or perhaps
 * multiple partitions.
 *
 * <p>The following aspects are verified:
 *
 * <ul>
 *   <li>the serialization format version, when specified, is a known one
 *   <li>the languages listed in the chunk are well-formed and correspond exactly to the languages
 *       used in the chunk
 *   <li>all metapointers are complete and well-formed (language and key are valid identifiers,
 *       version is not empty)
 *   <li>node IDs, as well as IDs used to refer to parents, children, annotations, and reference
 *       targets, are valid
 *   <li>there are no duplicate node IDs
 *   <li>no node uses the same feature metapointer more than once
 *   <li>containments and annotations are the inverse of parent relationships
 *   <li>no node appears in multiple containments or annotations
 *   <li>there are no cycles in the containment hierarchy
 * </ul>
 *
 * <p>Children and annotations listed by a node do not need to be present in the chunk, as the
 * specification allows them to be outside the processed document. Use {@link
 * PartitionChunkValidator} to validate a chunk representing an entire partition.
 */
public class ChunkValidator extends Validator<SerializationChunk> {

  private static final Set<String> KNOWN_SERIALIZATION_FORMAT_VERSIONS = new HashSet<>();

  static {
    for (LionWebVersion lionWebVersion : LionWebVersion.values()) {
      KNOWN_SERIALIZATION_FORMAT_VERSIONS.add(lionWebVersion.getVersionString());
    }
  }

  @Override
  public @Nonnull ValidationResult validate(@Nonnull SerializationChunk chunk) {
    Objects.requireNonNull(chunk, "chunk should not be null");
    ValidationResult validationResult = new ValidationResult();

    checkSerializationFormatVersion(chunk, validationResult);

    // Prepare supporting structures
    Map<String, SerializedClassifierInstance> nodesByID = new HashMap<>();
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      // Verifying IDs are valid
      if (!IdUtils.isValidID(node.getID())) {
        validationResult.addError("Invalid node id: " + node.getID(), node.getID());
      }

      if (nodesByID.containsKey(node.getID())) {
        // Verifying there are no duplicate node ids
        validationResult.addError("Duplicate node id: " + node.getID(), node.getID());
      } else {
        nodesByID.put(node.getID(), node);
      }
    }

    checkLanguagesAndMetaPointers(chunk, validationResult);
    checkReferencedIDs(chunk, validationResult);
    checkContainmentHierarchy(chunk, nodesByID, validationResult);
    checkCycles(chunk, nodesByID, validationResult);

    return validationResult;
  }

  private void checkSerializationFormatVersion(
      @Nonnull SerializationChunk chunk, @Nonnull ValidationResult validationResult) {
    String serializationFormatVersion = chunk.getSerializationFormatVersion();
    if (serializationFormatVersion == null) {
      return;
    }
    if (!KNOWN_SERIALIZATION_FORMAT_VERSIONS.contains(serializationFormatVersion)) {
      validationResult.addError(
          "Unknown serialization format version: " + serializationFormatVersion);
    }
  }

  private void checkLanguagesAndMetaPointers(
      @Nonnull SerializationChunk chunk, @Nonnull ValidationResult validationResult) {
    // Check the languages declared in the chunk are well-formed
    Set<LanguageVersion> declaredLanguages = new HashSet<>();
    for (LanguageVersion language : chunk.getLanguages()) {
      if (language.getKey() == null || language.getVersion() == null) {
        validationResult.addError(
            "Invalid language: both key and version should be specified, found "
                + formatLanguage(language));
      } else if (!IdUtils.isValidID(language.getKey()) || language.getVersion().isEmpty()) {
        validationResult.addError(
            "Invalid language: the key should be a valid identifier and the version should not be"
                + " empty, found "
                + formatLanguage(language));
      }
      declaredLanguages.add(language);
    }

    // Check used languages and duplicate metapointers
    Set<LanguageVersion> usedLanguages = new HashSet<>();
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      MetaPointer classifier = node.getClassifier();
      if (classifier == null) {
        validationResult.addError("Node " + node.getID() + " has no classifier", node.getID());
      } else {
        checkMetaPointer(node, classifier, "classifier", validationResult);
        usedLanguages.add(classifier.getLanguageVersion());
      }

      List<MetaPointer> metaPointers = new ArrayList<>();
      node.getContainments().forEach(c -> metaPointers.add(c.getMetaPointer()));
      node.getReferences().forEach(r -> metaPointers.add(r.getMetaPointer()));
      node.getProperties().forEach(p -> metaPointers.add(p.getMetaPointer()));

      Set<MetaPointer> featureMetaPointers = new HashSet<>();
      for (MetaPointer metaPointer : metaPointers) {
        if (metaPointer == null) {
          validationResult.addError(
              "Node " + node.getID() + " has a feature without metapointer", node.getID());
          continue;
        }
        checkMetaPointer(node, metaPointer, "feature", validationResult);
        usedLanguages.add(metaPointer.getLanguageVersion());
        if (!featureMetaPointers.add(metaPointer)) {
          validationResult.addError(
              "Node " + node.getID() + " has duplicate feature metapointer: " + metaPointer,
              node.getID());
        }
      }
    }

    if (!usedLanguages.equals(declaredLanguages)) {
      Set<String> extraLanguages = new TreeSet<>();
      for (LanguageVersion language : declaredLanguages) {
        if (!usedLanguages.contains(language)) {
          extraLanguages.add(formatLanguage(language));
        }
      }

      Set<String> missingLanguages = new TreeSet<>();
      for (LanguageVersion language : usedLanguages) {
        if (!declaredLanguages.contains(language)) {
          missingLanguages.add(formatLanguage(language));
        }
      }

      validationResult.addError(
          "We expected these used languages: "
              + usedLanguages
              + " and we found "
              + chunk.getLanguages()
              + ". Extra languages: "
              + String.join(", ", extraLanguages)
              + ". Missing languages: "
              + String.join(", ", missingLanguages));
    }
  }

  private static void checkMetaPointer(
      @Nonnull SerializedClassifierInstance node,
      @Nonnull MetaPointer metaPointer,
      @Nonnull String role,
      @Nonnull ValidationResult validationResult) {
    if (metaPointer.getLanguage() == null
        || metaPointer.getVersion() == null
        || metaPointer.getKey() == null) {
      validationResult.addError(
          "Node "
              + node.getID()
              + " has an incomplete "
              + role
              + " metapointer: "
              + metaPointer
              + ". Language, version, and key should all be specified",
          node.getID());
    } else if (!IdUtils.isValidID(metaPointer.getLanguage())
        || !IdUtils.isValidID(metaPointer.getKey())
        || metaPointer.getVersion().isEmpty()) {
      validationResult.addError(
          "Node "
              + node.getID()
              + " has an invalid "
              + role
              + " metapointer: "
              + metaPointer
              + ". Language and key should be valid identifiers, version should not be empty",
          node.getID());
    }
  }

  private void checkReferencedIDs(
      @Nonnull SerializationChunk chunk, @Nonnull ValidationResult validationResult) {
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      String parentID = node.getParentNodeID();
      if (parentID != null) {
        if (!IdUtils.isValidID(parentID)) {
          validationResult.addError(
              "Node " + node.getID() + " has an invalid parent id: " + parentID, node.getID());
        }
        if (parentID.equals(node.getID())) {
          validationResult.addError(
              "Node " + node.getID() + " lists itself as parent", node.getID());
        }
      }
      for (SerializedContainmentValue containmentValue : node.getContainments()) {
        for (String childId : containmentValue.getChildrenIds()) {
          if (!IdUtils.isValidID(childId)) {
            validationResult.addError(
                "Node " + node.getID() + " has an invalid child id: " + childId, node.getID());
          }
        }
      }
      for (String annotationId : node.getAnnotations()) {
        if (!IdUtils.isValidID(annotationId)) {
          validationResult.addError(
              "Node " + node.getID() + " has an invalid annotation id: " + annotationId,
              node.getID());
        }
      }
      for (SerializedReferenceValue referenceValue : node.getReferences()) {
        for (SerializedReferenceValue.Entry entry : referenceValue.getValue()) {
          String target = entry.getReference();
          if (target != null && !IdUtils.isValidID(target)) {
            validationResult.addError(
                "Node " + node.getID() + " has an invalid reference target id: " + target,
                node.getID());
          }
        }
      }
    }
  }

  private void checkContainmentHierarchy(
      @Nonnull SerializationChunk chunk,
      @Nonnull Map<String, SerializedClassifierInstance> nodesByID,
      @Nonnull ValidationResult validationResult) {
    // Ensuring that containments and annotations are the inverse of parent relationships
    Map<String, Set<String>> containedNodes = new HashMap<>();
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      for (SerializedContainmentValue containmentValue : node.getContainments()) {
        for (String childId : containmentValue.getChildrenIds()) {
          // Verifying nodes do not appear in multiple containments or annotations
          String newPlacement = node.getID() + " at " + containmentValue.getMetaPointer();
          registerPlacement(childId, newPlacement, containedNodes, validationResult);
          SerializedClassifierInstance child = nodesByID.get(childId);
          if (child != null && !Objects.equals(child.getParentNodeID(), node.getID())) {
            validationResult.addError(
                childId
                    + " is listed as child of "
                    + node.getID()
                    + " but it has as parent "
                    + child.getParentNodeID(),
                node.getID());
          }
        }
      }
      for (String annotationId : node.getAnnotations()) {
        // Verifying nodes do not appear in multiple containments or annotations
        String newPlacement = node.getID() + " among annotations";
        registerPlacement(annotationId, newPlacement, containedNodes, validationResult);
        SerializedClassifierInstance annotation = nodesByID.get(annotationId);
        if (annotation != null && !Objects.equals(annotation.getParentNodeID(), node.getID())) {
          validationResult.addError(
              annotationId
                  + " is listed as an annotation of "
                  + node.getID()
                  + " but it has as parent "
                  + annotation.getParentNodeID(),
              node.getID());
        }
      }

      if (node.getParentNodeID() != null && node.getID() != null) {
        SerializedClassifierInstance parent = nodesByID.get(node.getParentNodeID());
        if (parent != null && !parent.contains(node.getID())) {
          validationResult.addError(
              node.getID()
                  + " list as parent "
                  + node.getParentNodeID()
                  + " but such parent does not contain it",
              node.getID());
        }
      }
    }
  }

  private static void registerPlacement(
      @Nullable String containedId,
      @Nonnull String newPlacement,
      @Nonnull Map<String, Set<String>> containedNodes,
      @Nonnull ValidationResult validationResult) {
    Set<String> placements = containedNodes.get(containedId);
    if (placements != null) {
      validationResult.addError(
          containedId + " is listed in multiple places: " + placements + " and now " + newPlacement,
          containedId);
      placements.add(newPlacement);
    } else {
      containedNodes.put(containedId, new LinkedHashSet<>(Collections.singletonList(newPlacement)));
    }
  }

  /**
   * Verify that, following the parent relationships of the nodes present in the chunk, we never get
   * back to a node already visited. Self-parenting is reported separately.
   */
  private void checkCycles(
      @Nonnull SerializationChunk chunk,
      @Nonnull Map<String, SerializedClassifierInstance> nodesByID,
      @Nonnull ValidationResult validationResult) {
    // IDs of nodes for which we have already established whether they are part of a cycle
    Set<String> explored = new HashSet<>();
    Set<String> reportedInCycle = new HashSet<>();
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      if (node.getID() == null || explored.contains(node.getID())) {
        continue;
      }
      List<String> path = new ArrayList<>();
      Set<String> onPath = new HashSet<>();
      String current = node.getID();
      while (current != null && !explored.contains(current) && onPath.add(current)) {
        path.add(current);
        SerializedClassifierInstance currentNode = nodesByID.get(current);
        current = currentNode == null ? null : currentNode.getParentNodeID();
      }
      if (current != null && onPath.contains(current)) {
        List<String> cycle = path.subList(path.indexOf(current), path.size());
        // Self-parenting is reported elsewhere
        if (cycle.size() > 1 && reportedInCycle.addAll(cycle)) {
          validationResult.addError(
              "Cycle in the containment hierarchy: " + String.join(" -> ", cycle), current);
        }
      }
      explored.addAll(path);
    }
  }

  private static @Nonnull String formatLanguage(@Nonnull LanguageVersion language) {
    return language.getKey() + "/" + language.getVersion();
  }
}
