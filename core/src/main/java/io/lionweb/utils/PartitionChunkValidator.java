package io.lionweb.utils;

import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedContainmentValue;
import java.util.Comparator;
import java.util.Map;
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
  protected void additionalChecks(
      @Nonnull SerializationChunk chunk,
      @Nonnull Map<String, SerializedClassifierInstance> nodesByID,
      @Nonnull ValidationResult validationResult) {
    Set<String> roots = new TreeSet<>(Comparator.nullsFirst(Comparator.naturalOrder()));
    for (SerializedClassifierInstance node : chunk.getClassifierInstances()) {
      // All contained nodes are present in the chunk
      for (SerializedContainmentValue containmentValue : node.getContainments()) {
        for (String childId : containmentValue.getChildrenIds()) {
          if (!nodesByID.containsKey(childId)) {
            validationResult.addError("Missing node: " + childId, childId);
          }
        }
      }
      for (String annotationId : node.getAnnotations()) {
        if (!nodesByID.containsKey(annotationId)) {
          validationResult.addError("Missing node: " + annotationId, annotationId);
        }
      }
      if (node.getParentNodeID() == null) {
        roots.add(node.getID());
      }
    }

    // There is just one root in the chunk
    if (roots.size() != 1) {
      validationResult.addError("Expected exactly one root, found: " + roots);
    }
  }
}
