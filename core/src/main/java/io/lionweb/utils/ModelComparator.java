package io.lionweb.utils;

import io.lionweb.language.*;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.model.ReferenceValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModelComparator {

  public static class ComparisonResult {
    private final List<String> differences = new ArrayList<>();

    public List<String> getDifferences() {
      return differences;
    }

    public boolean areEquivalent() {
      return differences.isEmpty();
    }

    public void markDifferentIDs(String context, String idA, String idB) {
      differences.add(context + ": different ids, a=" + idA + ", b=" + idB);
    }

    public void markDifferentAnnotated(String context, String idA, String idB) {
      differences.add(context + ": different annotated ids, a=" + idA + ", b=" + idB);
    }

    public void markDifferentConcept(
        String context, String nodeID, String conceptIDa, String conceptIDb) {
      differences.add(
          context
              + " (id="
              + nodeID
              + ") : different concepts, a="
              + conceptIDa
              + ", b="
              + conceptIDb);
    }

    public void markDifferentPropertyValue(
        String context, String nodeID, String propertyName, Object valueA, Object valueB) {
      differences.add(
          context
              + " (id="
              + nodeID
              + ") : different property value for "
              + propertyName
              + ", a="
              + valueA
              + ", b="
              + valueB);
    }

    public void markDifferentNumberOfChildren(
        String context, String nodeID, String containmentName, int childrenA, int childrenB) {
      differences.add(
          context
              + " (id="
              + nodeID
              + ") : different number of children for "
              + containmentName
              + ", a="
              + childrenA
              + ", b="
              + childrenB);
    }

    public void markDifferentNumberOfReferences(
        String context, String nodeID, String referenceName, int childrenA, int childrenB) {
      differences.add(
          context
              + " (id="
              + nodeID
              + ") : different number of referred for "
              + referenceName
              + ", a="
              + childrenA
              + ", b="
              + childrenB);
    }

    public void markDifferentReferredID(
        String context,
        String nodeID,
        String referenceName,
        int index,
        String referredA,
        String referredB) {
      differences.add(
          context
              + " (id="
              + nodeID
              + ") : different referred id for "
              + referenceName
              + " index "
              + index
              + ", a="
              + referredA
              + ", b="
              + referredB);
    }

    public void markDifferentResolveInfo(
        String context,
        String nodeID,
        String referenceName,
        int index,
        String resolveInfoA,
        String resolveInfoB) {
      differences.add(
          context
              + " (id="
              + nodeID
              + ") : different resolve info for "
              + referenceName
              + " index "
              + index
              + ", a="
              + resolveInfoA
              + ", b="
              + resolveInfoB);
    }

    @Override
    public String toString() {
      return "ComparisonResult: " + differences;
    }

    public ComparisonResult markIncompatible() {
      differences.add("incompatible instances");
      return this;
    }

    public void markDifferentNumberOfAnnotations(String context, int na, int nb) {
      differences.add(context + " different number of annotations (" + na + " != " + nb + ")");
    }

    public void markDifferentAnnotation(String context, int i) {
      differences.add(context + " annotation " + i + " is different");
    }
  }

  /**
   * Determines if two {@link ClassifierInstance} objects are equivalent by comparing their models.
   *
   * @param a the first {@link ClassifierInstance} object to compare
   * @param b the second {@link ClassifierInstance} object to compare
   * @return {@code true} if the two {@link ClassifierInstance} objects are equivalent, {@code
   *     false} otherwise
   */
  public static boolean areEquivalent(ClassifierInstance<?> a, ClassifierInstance<?> b) {
    ModelComparator modelComparator = new ModelComparator();
    ModelComparator.ComparisonResult comparisonResult = modelComparator.compare(a, b);
    return comparisonResult.areEquivalent();
  }

  /**
   * Compares two lists of {@link ClassifierInstance} objects to determine if they are equivalent.
   * Two lists are considered equivalent if they have the same size and all corresponding elements
   * in the lists are equivalent, based on the {@code areEquivalent} method for individual {@link
   * ClassifierInstance} objects.
   *
   * @param as the first list of {@link ClassifierInstance} objects to compare
   * @param bs the second list of {@link ClassifierInstance} objects to compare
   * @return {@code true} if the two lists are equivalent, {@code false} otherwise
   */
  public static <A extends ClassifierInstance<?>, B extends ClassifierInstance<?>>
      boolean areEquivalent(List<A> as, List<B> bs) {
    if (as.size() != bs.size()) {
      return false;
    }
    for (int i = 0; i < as.size(); i++) {
      if (!areEquivalent(as.get(i), bs.get(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares two nodes and determines their differences.
   *
   * <p>This method compares the properties, structure, and key attributes of two nodes and returns
   * a {@link ComparisonResult} object, which contains details about any discrepancies found between
   * the nodes.
   *
   * @param nodeA the first node to compare
   * @param nodeB the second node to compare
   * @return a {@link ComparisonResult} object containing the results of the comparison
   */
  public ComparisonResult compare(Node nodeA, Node nodeB) {
    ComparisonResult comparisonResult = new ComparisonResult();
    compare(nodeA, nodeB, comparisonResult, "<root>");
    return comparisonResult;
  }

  /**
   * Compares two {@link AnnotationInstance} objects and determines their differences.
   *
   * <p>This method analyzes the properties and attributes of two {@link AnnotationInstance} objects
   * and generates a {@link ComparisonResult} that contains details about discrepancies identified
   * during the comparison.
   *
   * @param nodeA the first {@link AnnotationInstance} object to compare
   * @param nodeB the second {@link AnnotationInstance} object to compare
   * @return a {@link ComparisonResult} object that encapsulates the results of the comparison
   */
  public ComparisonResult compare(AnnotationInstance nodeA, AnnotationInstance nodeB) {
    ComparisonResult comparisonResult = new ComparisonResult();
    compare(nodeA, nodeB, comparisonResult, "<root>");
    return comparisonResult;
  }

  /**
   * Compares two {@link ClassifierInstance} objects and determines their differences based on their
   * specific types and attributes.
   *
   * <p>If both objects are instances of {@link Node}, they are compared using the node-specific
   * comparison. If both objects are instances of {@link AnnotationInstance}, they are compared
   * using the annotation-specific comparison. If the objects are of incompatible types, the result
   * is marked as incompatible.
   *
   * @param nodeA the first {@link ClassifierInstance} object to compare
   * @param nodeB the second {@link ClassifierInstance} object to compare
   * @return a {@link ComparisonResult} object containing the results of the comparison, or marked
   *     as incompatible if the two objects cannot be compared.
   */
  public ComparisonResult compare(ClassifierInstance<?> nodeA, ClassifierInstance<?> nodeB) {
    if (nodeA instanceof Node && nodeB instanceof Node) {
      return compare((Node) nodeA, (Node) nodeB);
    } else if (nodeA instanceof AnnotationInstance && nodeB instanceof AnnotationInstance) {
      return compare((AnnotationInstance) nodeA, (AnnotationInstance) nodeB);
    } else {
      return new ComparisonResult().markIncompatible();
    }
  }

  private void compareProperties(
      Classifier<?> concept,
      ClassifierInstance<?> nodeA,
      ClassifierInstance<?> nodeB,
      ComparisonResult comparisonResult,
      String context) {
    for (Property property : concept.allProperties()) {
      Object valueA = nodeA.getPropertyValue(property);
      Object valueB = nodeB.getPropertyValue(property);
      if (!Objects.equals(valueA, valueB)) {
        comparisonResult.markDifferentPropertyValue(
            context, nodeA.getID(), property.qualifiedName(), valueA, valueB);
      }
    }
  }

  private void compareReferences(
      Classifier<?> concept,
      ClassifierInstance<?> nodeA,
      ClassifierInstance<?> nodeB,
      ComparisonResult comparisonResult,
      String context) {
    for (Reference reference : concept.allReferences()) {
      List<ReferenceValue> valueA = nodeA.getReferenceValues(reference);
      List<ReferenceValue> valueB = nodeB.getReferenceValues(reference);
      if (valueA.size() != valueB.size()) {
        comparisonResult.markDifferentNumberOfReferences(
            context, nodeA.getID(), reference.qualifiedName(), valueA.size(), valueB.size());
      } else {
        for (int i = 0; i < valueA.size(); i++) {
          ReferenceValue refA = valueA.get(i);
          ReferenceValue refB = valueB.get(i);
          if (!Objects.equals(refA.getReferredID(), refB.getReferredID())) {
            comparisonResult.markDifferentReferredID(
                context,
                nodeA.getID(),
                reference.qualifiedName(),
                i,
                refA.getReferredID(),
                refB.getReferredID());
          }
          if (!Objects.equals(refA.getResolveInfo(), refB.getResolveInfo())) {
            comparisonResult.markDifferentResolveInfo(
                context,
                nodeA.getID(),
                reference.qualifiedName(),
                i,
                refA.getResolveInfo(),
                refB.getResolveInfo());
          }
        }
      }
    }
  }

  private void compareContainments(
      Classifier<?> concept,
      ClassifierInstance<?> nodeA,
      ClassifierInstance<?> nodeB,
      ComparisonResult comparisonResult,
      String context) {
    for (Containment containment : concept.allContainments()) {
      List<? extends Node> valueA = nodeA.getChildren(containment);
      List<? extends Node> valueB = nodeB.getChildren(containment);
      if (valueA.size() != valueB.size()) {
        comparisonResult.markDifferentNumberOfChildren(
            context, nodeA.getID(), containment.qualifiedName(), valueA.size(), valueB.size());
      } else {
        for (int i = 0; i < valueA.size(); i++) {
          compare(
              valueA.get(i),
              valueB.get(i),
              comparisonResult,
              context + "/" + containment.getName() + "[" + i + "]");
        }
      }
    }
  }

  private void compareAnnotations(
      Classifier<?> concept,
      ClassifierInstance<?> nodeA,
      ClassifierInstance<?> nodeB,
      ComparisonResult comparisonResult,
      String context) {
    if (nodeA.getAnnotations().size() != nodeB.getAnnotations().size()) {
      comparisonResult.markDifferentNumberOfAnnotations(
          context, nodeA.getAnnotations().size(), nodeB.getAnnotations().size());
    }
    for (int i = 0;
        i < Math.min(nodeA.getAnnotations().size(), nodeB.getAnnotations().size());
        i++) {
      String aID = nodeA.getAnnotations().get(i).getID();
      String bID = nodeB.getAnnotations().get(i).getID();
      if (!Objects.equals(aID, bID)) {
        comparisonResult.markDifferentAnnotation(context, i);
      }
    }
  }

  private void compare(Node nodeA, Node nodeB, ComparisonResult comparisonResult, String context) {
    if (!Objects.equals(nodeA.getID(), nodeB.getID())) {
      comparisonResult.markDifferentIDs(context, nodeA.getID(), nodeB.getID());
    } else {
      if (Objects.equals(nodeA.getClassifier().getID(), nodeB.getClassifier().getID())) {
        Concept concept = nodeA.getClassifier();
        compareProperties(concept, nodeA, nodeB, comparisonResult, context);
        compareReferences(concept, nodeA, nodeB, comparisonResult, context);
        compareContainments(concept, nodeA, nodeB, comparisonResult, context);
        compareAnnotations(concept, nodeA, nodeB, comparisonResult, context);
      } else {
        comparisonResult.markDifferentConcept(
            context, nodeA.getID(), nodeA.getClassifier().getID(), nodeB.getClassifier().getID());
      }
    }
  }

  private void compare(
      AnnotationInstance nodeA,
      AnnotationInstance nodeB,
      ComparisonResult comparisonResult,
      String context) {
    if (!Objects.equals(nodeA.getID(), nodeB.getID())) {
      comparisonResult.markDifferentIDs(context, nodeA.getID(), nodeB.getID());
    } else {
      if (Objects.equals(
          nodeA.getAnnotationDefinition().getID(), nodeB.getAnnotationDefinition().getID())) {
        Annotation concept = nodeA.getAnnotationDefinition();
        if (!Objects.equals(nodeA.getParent().getID(), nodeB.getParent().getID())) {
          comparisonResult.markDifferentAnnotated(context, nodeA.getID(), nodeB.getID());
        }
        compareProperties(concept, nodeA, nodeB, comparisonResult, context);
        compareReferences(concept, nodeA, nodeB, comparisonResult, context);
        compareContainments(concept, nodeA, nodeB, comparisonResult, context);
        compareAnnotations(concept, nodeA, nodeB, comparisonResult, context);
      } else {
        comparisonResult.markDifferentConcept(
            context, nodeA.getID(), nodeA.getClassifier().getID(), nodeB.getClassifier().getID());
      }
    }
  }
}
