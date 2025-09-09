package io.lionweb.utils;

import static java.util.stream.Collectors.*;

import io.lionweb.serialization.data.SerializedChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedContainmentValue;
import java.util.Set;

public class PartitionChunkValidator extends ChunkValidator {

  @Override
  public ValidationResult validate(SerializedChunk chunk) {
    ValidationResult validationResult = super.validate(chunk);

    // All contained nodes are present in the chunk
    Set<String> nodesPresent =
        chunk.getClassifierInstances().stream().map(n -> n.getID()).collect(toSet());
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      for (SerializedContainmentValue containmentValue : node.getContainments()) {
        for (String childId : containmentValue.getValue()) {
          // We do not set the subject as the subject is not a node
          validationResult.checkForError(
              !nodesPresent.contains(childId), "Missing node: " + childId, null);
        }
      }
      for (String annotationId : node.getAnnotations()) {
        // We do not set the subject as the subject is not a node
        validationResult.checkForError(
            !nodesPresent.contains(annotationId), "Missing node: " + annotationId, null);
      }
    }

    // There is just one root in the chunk
    Set<String> roots =
        chunk.getClassifierInstances().stream()
            .filter(n -> n.getParentNodeID() == null)
            .map(n -> n.getID())
            .collect(toSet());
    // We do not set the subject as the subject is not a node
    validationResult.checkForError(
        roots.size() != 1, "Expected exactly one root, found: " + roots, null);

    return validationResult;
  }
}
