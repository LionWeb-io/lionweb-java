package io.lionweb.lioncore.java.utils;

import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import java.util.HashMap;
import java.util.Map;

public class NodeTreeValidator extends Validator<Node> {
  @Override
  public ValidationResult validate(Node element) {
    ValidationResult validationResult = new ValidationResult();
    validateNodeAndDescendants(element, validationResult);
    validateIDsAreUnique(element, validationResult);
    return validationResult;
  }

  private void validateNodeAndDescendants(Node node, ValidationResult validationResult) {
    if (node.getID() != null) {
      // It does not make sense to make the same ID as null and invalid
      validationResult.checkForError(!CommonChecks.isValidID(node.getID()), "Invalid ID", node);
    }
    if (node.isRoot()) {
      validationResult.checkForError(
          !node.getClassifier().isPartition(),
          "A root node should be an instance of a Partition concept",
          node);
    }
    ClassifierInstanceUtils.getChildren(node)
        .forEach(child -> validateNodeAndDescendants(child, validationResult));
  }

  private void validateIDsAreUnique(Node node, ValidationResult result) {
    Map<String, Node> uniqueIDs = new HashMap<>();
    node.thisAndAllDescendants()
        .forEach(
            n -> {
              String id = n.getID();
              if (id != null) {
                if (uniqueIDs.containsKey(id)) {
                  result.addError(
                      "ID " + id + " is duplicate. It is also used by " + uniqueIDs.get(id), n);
                } else {
                  uniqueIDs.put(id, n);
                }
              } else {
                result.addError("ID null found", n);
              }
            });
  }
}
