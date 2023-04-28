package org.lionweb.lioncore.java.utils;

import java.util.HashMap;
import java.util.Map;
import org.lionweb.lioncore.java.model.Node;

public class NodeTreeValidator extends Validator<Node> {
  @Override
  public ValidationResult validate(Node element) {
    ValidationResult validationResult = new ValidationResult();
    validateNodeAndDescendants(element, validationResult);
    validateNodeAndDescendants(element, validationResult);
    return validationResult;
  }

  private void validateNodeAndDescendants(Node node, ValidationResult validationResult) {
    validationResult.checkForError(!CommonChecks.isValidID(node.getID()), "Invalid ID", node);
    node.getChildren().forEach(child -> validateNodeAndDescendants(child, validationResult));
  }

  private void validateIDsAreUnique(Node node, ValidationResult result) {
    Map<String, String> uniqueIDs = new HashMap<>();
    node.thisAndAllDescendants()
        .forEach(
            n -> {
              String id = node.getID();
              if (id != null) {
                if (uniqueIDs.containsKey(id)) {
                  result.addError(
                      "ID " + id + " is duplicate. It is also used by " + uniqueIDs.get(id), n);
                } else {
                  uniqueIDs.put(id, n.getID());
                }
              }
            });
  }
}
