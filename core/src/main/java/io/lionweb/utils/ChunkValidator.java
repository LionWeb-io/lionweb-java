package io.lionweb.utils;

import io.lionweb.serialization.data.*;
import java.util.*;

/**
 * Validate a generic chunk, which may contain part of a partition, an entire partition, or perhaps
 * multiple partitions.
 */
public class ChunkValidator extends Validator<SerializedChunk> {
  @Override
  public ValidationResult validate(SerializedChunk chunk) {
    ValidationResult validationResult = new ValidationResult();

    // Prepare supporting structures
    Map<String, SerializedClassifierInstance> nodesByID = new HashMap<>();
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      // Verifying IDs are valid
      if (!CommonChecks.isValidID(node.getID())) {
        // We do not set the subject as the subject is not a node
        validationResult.addError("Invalid node id: " + node.getID(), null);
      }

      if (nodesByID.containsKey(node.getID())) {
        // Verifying there are no duplicate node ids
        // We do not set the subject as the subject is not a node
        validationResult.addError("Duplicate node id: " + node.getID(), null);
      } else {
        nodesByID.put(node.getID(), node);
      }
    }

    // Check languages
    Set<UsedLanguage> usedLanguages = new HashSet<>();
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
      // We do not set the subject as the subject is not a node
      validationResult.addError(
          "We expected these used languages: "
              + usedLanguages
              + " and we found "
              + chunk.getLanguages(),
          null);
    }

    // Ensuring that containments + annotations are the inverse of parent relationships
    Set<String> containedNodes = new HashSet<>();
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      for (SerializedContainmentValue containmentValue : node.getContainments()) {
        for (String childId : containmentValue.getValue()) {
          // Verifying nodes do not appear in multiple containments or annotations
          if (containedNodes.contains(childId)) {
            // We do not set the subject as the subject is not a node
            validationResult.addError(childId + " is listed in multiple places", null);
          } else {
            containedNodes.add(childId);
          }
          SerializedClassifierInstance child = nodesByID.get(childId);
          if (child != null && !child.getParentNodeID().equals(node.getID())) {
            // We do not set the subject as the subject is not a node
            validationResult.addError(
                childId
                    + " is listed as child of "
                    + node.getID()
                    + " but it has as parent "
                    + child.getParentNodeID(),
                null);
          }
        }
      }
      for (String annotationId : node.getAnnotations()) {
        // Verifying nodes do not appear in multiple containments or annotations
        if (containedNodes.contains(annotationId)) {
          // We do not set the subject as the subject is not a node
          validationResult.addError(annotationId + " is listed in multiple places", null);
        } else {
          containedNodes.add(annotationId);
        }
        SerializedClassifierInstance annotation = nodesByID.get(annotationId);
        if (annotationId != null && !annotation.getParentNodeID().equals(node.getID())) {
          // We do not set the subject as the subject is not a node
          validationResult.addError(
              annotationId
                  + " is listed as an annotation of "
                  + node.getID()
                  + " but it has as parent "
                  + annotation.getParentNodeID(),
              null);
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
              null);
        }
      }
    }

    return validationResult;
  }
}
