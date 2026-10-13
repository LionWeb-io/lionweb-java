package io.lionweb.utils;

import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedContainmentValue;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Verify a Chunk represents a valid partition. It does all the checks done by ChunkValidator, plus
 * some checks specific to partitions: all contained nodes are present in the chunk and there is
 * exactly one root.
 */
public class PartitionChunkValidator extends ChunkValidator {

  @Override
  public @Nonnull ValidationResult validate(@Nonnull SerializationChunk chunk) {
    Objects.requireNonNull(chunk, "chunk should not be null");
    ValidationResult validationResult = super.validate(chunk);

    // All contained nodes are present in the chunk
    Set<String> nodesPresent =
        chunk.getClassifierInstances().stream()
            .map(SerializedClassifierInstance::getID)
            .collect(Collectors.toSet());
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      for (SerializedContainmentValue containmentValue : node.getContainments()) {
        for (String childId : containmentValue.getChildrenIds()) {
          validationResult.addErrorIf(
              !nodesPresent.contains(childId), "Missing node: " + childId, childId);
        }
      }
      for (String annotationId : node.getAnnotations()) {
        validationResult.addErrorIf(
            !nodesPresent.contains(annotationId), "Missing node: " + annotationId, annotationId);
      }
    }

    // There is just one root in the chunk
    Set<String> roots = new TreeSet<>(Comparator.nullsFirst(Comparator.naturalOrder()));
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      if (node.getParentNodeID() == null) {
        roots.add(node.getID());
      }
    }
    validationResult.addErrorIf(roots.size() != 1, "Expected exactly one root, found: " + roots);

    return validationResult;
  }
}
