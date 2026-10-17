package io.lionweb.utils;

import io.lionweb.language.Classifier;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * The {@code NodeTreeValidator} class is responsible for validating a hierarchy of {@link Node}
 * instances. It performs a set of checks to ensure the structural, semantic, and uniqueness
 * correctness of {@link Node} trees.
 *
 * <ul>
 *   <li>It validates each node's ID to ensure it conforms to predefined rules.
 *   <li>It ensures root nodes, and only root nodes, are instances of a partition concept.
 *   <li>It checks containment rules, such as required and single features, are correctly satisfied.
 *   <li>It checks that annotations are attached only to instances of the classifier they annotate.
 *   <li>It verifies that all node IDs within the hierarchy are unique.
 * </ul>
 */
public class NodeTreeValidator extends Validator<Node> {
  @Override
  public @Nonnull ValidationResult validate(@Nonnull Node element) {
    ValidationResult validationResult = new ValidationResult();
    validateNodeAndDescendants(element, validationResult);
    validateIDsAreUnique(element, validationResult);
    return validationResult;
  }

  private void validateNodeAndDescendants(Node node, ValidationResult validationResult) {
    if (node.getID() != null) {
      // It does not make sense to make the same ID as null and invalid
      validationResult.addErrorIf(!IdUtils.isValidID(node.getID()), "Invalid ID", node);
    }
    if (node.isRoot()) {
      validationResult.addErrorIf(
          !node.getClassifier().isPartition(),
          "A root node should be an instance of a Partition concept",
          node);
    } else {
      validationResult.addErrorIf(
          node.getClassifier().isPartition(),
          "A non-root node should not be an instance of a Partition concept",
          node);
    }
    node.getClassifier()
        .allContainments()
        .forEach(
            containment -> {
              int actualNChildren = node.getChildren(containment).size();
              validationResult.addErrorIf(
                  containment.isRequired() && actualNChildren == 0,
                  "Containment "
                      + containment.getName()
                      + " is required but no children are specified",
                  node);
              validationResult.addErrorIf(
                  containment.isSingle() && actualNChildren > 1,
                  "Containment "
                      + containment.getName()
                      + " is single but it has "
                      + actualNChildren
                      + " children",
                  node);
            });
    for (AnnotationInstance annotationInstance : node.getAnnotations()) {
      Classifier<?> annotatedClassifier =
          annotationInstance.getAnnotationDefinition().getEffectivelyAnnotated();
      // A missing annotates is a language error, reported by the LanguageValidator
      if (annotatedClassifier != null
          && !isSameOrSubtypeOf(node.getClassifier(), annotatedClassifier)) {
        validationResult.addError(
            "Annotation "
                + annotationInstance.getAnnotationDefinition().getName()
                + " cannot be applied to an instance of "
                + node.getClassifier().getName()
                + ", as it annotates "
                + annotatedClassifier.getName(),
            annotationInstance);
      }
    }
    ClassifierInstanceUtils.getChildren(node)
        .forEach(child -> validateNodeAndDescendants(child, validationResult));
  }

  /** Classifiers are compared by ID, as their equals compares their whole content. */
  private static boolean isSameOrSubtypeOf(Classifier<?> classifier, Classifier<?> other) {
    return Objects.equals(classifier.getID(), other.getID())
        || inheritsFrom(classifier, other.getID(), new HashSet<>());
  }

  /** Stops as soon as a match is found. The visited set guards against inheritance loops. */
  private static boolean inheritsFrom(
      Classifier<?> classifier, String otherID, Set<String> visitedIDs) {
    if (!visitedIDs.add(classifier.getID())) {
      return false;
    }
    for (Classifier<?> ancestor : classifier.directAncestors()) {
      if (Objects.equals(ancestor.getID(), otherID)
          || inheritsFrom(ancestor, otherID, visitedIDs)) {
        return true;
      }
    }
    return false;
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
