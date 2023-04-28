package org.lionweb.lioncore.java.utils;

import org.lionweb.lioncore.java.model.Node;

public class NodeTreeValidator extends Validator<Node> {
  @Override
  public ValidationResult validate(Node element) {
    ValidationResult validationResult = new ValidationResult();
    validateNodeAndDescendants(element, validationResult);
    return validationResult;
  }

  private void validateNodeAndDescendants(Node node, ValidationResult validationResult) {
    validationResult.checkForError(!CommonChecks.isValidID(node.getID()), "Invalid ID", node);
    node.getChildren().forEach(child -> validateNodeAndDescendants(child, validationResult));
  }
}
