package io.lionweb.utils;

import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedContainmentValue;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
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
    Set<String> nodesPresent = new HashSet<>();
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      nodesPresent.add(node.getID());
    }
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      for (SerializedContainmentValue containmentValue : node.getContainments()) {
        for (String childId : containmentValue.getChildrenIds()) {
          if (!nodesPresent.contains(childId)) {
            validationResult.addError("Missing node: " + childId, childId);
          }
        }
      }
      for (String annotationId : node.getAnnotations()) {
        if (!nodesPresent.contains(annotationId)) {
          validationResult.addError("Missing node: " + annotationId, annotationId);
        }
      }
    }

    // There is just one root in the chunk
    Set<String> roots = new TreeSet<>(Comparator.nullsFirst(Comparator.naturalOrder()));
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      if (node.getParentNodeID() == null) {
        roots.add(node.getID());
      }
    }
    if (roots.size() != 1) {
      validationResult.addError("Expected exactly one root, found: " + roots);
    }

    return validationResult;
  }
}
