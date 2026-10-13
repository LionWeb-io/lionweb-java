package io.lionweb.utils;

import static java.util.stream.Collectors.*;

import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.serialization.data.SerializedContainmentValue;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Verify a Chunk represents a valid partition. It does all the checks done by ChunkValidator, plus
 * some checks specific to partitions, such as requiring exactly one root.
 */
public class PartitionChunkValidator extends ChunkValidator {

  public PartitionChunkValidator() {
    // A partition has exactly one root
    super(true);
  }

  @Override
  public @Nonnull ValidationResult validate(@Nonnull SerializationChunk chunk) {
    Objects.requireNonNull(chunk, "chunk should not be null");
    ValidationResult validationResult = super.validate(chunk);

    // All contained nodes are present in the chunk
    Set<String> nodesPresent =
        chunk.getClassifierInstances().stream().map(n -> n.getID()).collect(toSet());
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

    return validationResult;
  }
}
