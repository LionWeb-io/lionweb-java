package io.lionweb.utils;

import io.lionweb.serialization.data.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Validate a generic chunk, which may contain part of a partition, an entire partition, or perhaps
 * multiple partitions.
 */
public class ChunkValidator extends Validator<SerializationChunk> {
  @Override
  public ValidationResult validate(@Nonnull SerializationChunk chunk) {
    Objects.requireNonNull(chunk, "chunk should not be null");
    ValidationResult validationResult = new ValidationResult();

    // Prepare supporting structures
    Map<String, SerializedClassifierInstance> nodesByID = new HashMap<>();
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      // Verifying IDs are valid
      if (!CommonChecks.isValidID(node.getID())) {
        validationResult.addError("Invalid node id: " + node.getID(), node.getID());
      }

      if (nodesByID.containsKey(node.getID())) {
        // Verifying there are no duplicate node ids
        validationResult.addError("Duplicate node id: " + node.getID(), node.getID());
      } else {
        nodesByID.put(node.getID(), node);
      }
    }

    // Check languages
    Set<LanguageVersion> usedLanguages = new HashSet<>();
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      usedLanguages.add(node.getClassifier().getUsedLanguage());
      for (SerializedPropertyValue propertyValue : node.getProperties()) {
        usedLanguages.add(propertyValue.getMetaPointer().getUsedLanguage());
      }
      for (SerializedReferenceValue referenceValue : node.getReferences()) {
        usedLanguages.add(referenceValue.getMetaPointer().getUsedLanguage());
      }
      for (SerializedContainmentValue containmentValue : node.getContainments()) {
        usedLanguages.add(containmentValue.getMetaPointer().getUsedLanguage());
      }
    }
    if (!usedLanguages.equals(new HashSet<>(chunk.getLanguages()))) {
      Function<LanguageVersion, String> languageFormatter =
          lang -> lang.getKey() + "/" + lang.getVersion();

      String extraLanguages =
          chunk.getLanguages().stream()
              .filter(e -> !usedLanguages.contains(e))
              .map(languageFormatter)
              .sorted()
              .collect(Collectors.joining(", "));

      String missingLanguages =
          usedLanguages.stream()
              .filter(e -> !chunk.getLanguages().contains(e))
              .map(languageFormatter)
              .sorted()
              .collect(Collectors.joining(", "));

      validationResult.addError(
          "We expected these used languages: "
              + usedLanguages
              + " and we found "
              + chunk.getLanguages()
              + ". Extra languages: "
              + extraLanguages
              + ". Missing languages: "
              + missingLanguages);
    }

    // Ensuring that containments and annotations are the inverse of parent relationships
    Map<String, Set<String>> containedNodes = new HashMap<>();
    Set<String> idsInChunk = new HashSet<>();
    Set<String> expectedBecauseContained = new HashSet<>();
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      idsInChunk.add(node.getID());
      for (SerializedContainmentValue containmentValue : node.getContainments()) {
        for (String childId : containmentValue.getChildrenIds()) {
          expectedBecauseContained.add(childId);
          // Verifying nodes do not appear in multiple containments or annotations
          String newPlacement = node.getID() + " at " + containmentValue.getMetaPointer();
          if (containedNodes.containsKey(childId)) {
            validationResult.addError(
                childId
                    + " is listed in multiple places: "
                    + containedNodes.get(node.getID())
                    + " and now "
                    + newPlacement,
                childId);
            containedNodes.get(childId).add(newPlacement);
          } else {
            containedNodes.put(childId, new HashSet<>(Arrays.asList(newPlacement)));
          }
          SerializedClassifierInstance child = nodesByID.get(childId);
          if (child != null && !child.getParentNodeID().equals(node.getID())) {
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
        expectedBecauseContained.add(annotationId);
        // Verifying nodes do not appear in multiple containments or annotations
        String newPlacement = node.getID() + " among annotations";
        if (containedNodes.containsKey(annotationId)) {
          validationResult.addError(annotationId + " is listed in multiple places", annotationId);
          containedNodes.get(annotationId).add(newPlacement);
        } else {
          containedNodes.put(annotationId, new HashSet<>(Arrays.asList(newPlacement)));
        }
        SerializedClassifierInstance annotation = nodesByID.get(annotationId);
        if (annotationId != null
            && annotation != null
            && !Objects.equals(annotation.getParentNodeID(), node.getID())) {
          validationResult.addError(
              annotationId
                  + " is listed as an annotation of "
                  + node.getID()
                  + " but it has as parent "
                  + annotation.getParentNodeID(),
              node.getID());
        }
      }

      if (node.getParentNodeID() != null) {
        SerializedClassifierInstance parent = nodesByID.get(node.getParentNodeID());
        if (parent != null && !parent.contains(node.getID())) {
          // We do not set the subject as the subject is not a node
          validationResult.addError(
              node.getID()
                  + " list as parent "
                  + node.getParentNodeID()
                  + " but such parent does not contain it",
              node.getID());
        }
      }
    }

    // Verify all contained nodes are present
    Set<String> missingNodes = new HashSet<>();
    for (String requiredId : expectedBecauseContained) {
      if (!idsInChunk.contains(requiredId)) {
        missingNodes.add(requiredId);
      }
    }
    if (!missingNodes.isEmpty()) {
      validationResult.addError(
          "Some nodes should be contained, but are not present: "
              + missingNodes.stream().sorted().collect(Collectors.joining(", ")));
    }

    return validationResult;
  }
}
