package io.lionweb.lioncore.java.utils;

import io.lionweb.lioncore.java.metamodel.*;
import io.lionweb.lioncore.java.metamodel.Enumeration;
import io.lionweb.lioncore.java.model.impl.M3Node;
import java.util.*;
import java.util.stream.Collectors;

public class MetamodelValidator extends Validator<Metamodel> {

  public static void ensureIsValid(Metamodel metamodel) {
    ValidationResult vr = new MetamodelValidator().validate(metamodel);
    if (!vr.isSuccessful()) {
      throw new RuntimeException("Invalid metamodel: " + vr.getIssues());
    }
  }

  @Override
  public ValidationResult validate(Metamodel metamodel) {
    // Given metamodels are also valid node trees, we check against errors for node trees
    ValidationResult result = new NodeTreeValidator().validate(metamodel);

    metamodel
        .thisAndAllDescendants()
        .forEach(
            n ->
                result.checkForError(
                    !CommonChecks.isValidID(n.getID()),
                    "Node IDs should respect the format for IDs",
                    n));

    metamodel
        .thisAndAllDescendants()
        .forEach(
            n -> {
              if (n instanceof HasKey<?>) {
                HasKey<?> hk = (HasKey<?>) n;
                result.checkForError(
                    !CommonChecks.isValidID(hk.getKey()),
                    "Keys should respect the format for IDs",
                    n);
              }
            });

    result.checkForError(metamodel.getName() == null, "Qualified name not set", metamodel);

    validateNamesAreUnique(metamodel.getElements(), result);

    // TODO once we implement the Node interface we could navigate the tree differently

    metamodel
        .getElements()
        .forEach(
            (MetamodelElement el) -> {
              result
                  .checkForError(el.getName() == null, "Simple name not set", el)
                  .checkForError(el.getMetamodel() == null, "Metamodel not set", el)
                  .checkForError(
                      el.getMetamodel() != null && el.getMetamodel() != metamodel,
                      "Metamodel not set correctly",
                      el);

              if (el instanceof io.lionweb.lioncore.java.metamodel.Enumeration) {
                io.lionweb.lioncore.java.metamodel.Enumeration enumeration =
                    (io.lionweb.lioncore.java.metamodel.Enumeration) el;
                enumeration
                    .getLiterals()
                    .forEach(
                        (EnumerationLiteral lit) ->
                            result.checkForError(
                                lit.getName() == null, "Simple name not set", lit));
                validateNamesAreUnique(enumeration.getLiterals(), result);
              }
              if (el instanceof FeaturesContainer) {
                FeaturesContainer<M3Node> featuresContainer = (FeaturesContainer) el;
                featuresContainer
                    .getFeatures()
                    .forEach(
                        (Feature feature) ->
                            result
                                .checkForError(
                                    feature.getName() == null, "Simple name not set", feature)
                                .checkForError(
                                    feature.getContainer() == null, "Container not set", feature)
                                .checkForError(
                                    feature.getContainer() != null
                                        && feature.getContainer() != featuresContainer,
                                    "Features container not set correctly",
                                    feature));
                validateNamesAreUnique(featuresContainer.getFeatures(), result);
              }
              if (el instanceof Concept) {
                Concept concept = (Concept) el;
                checkAncestors(concept, result);
                result.checkForError(
                    concept.getImplemented().size()
                        != concept.getImplemented().stream().distinct().count(),
                    "The same interface has been implemented multiple times",
                    concept);
              }
              if (el instanceof ConceptInterface) {
                checkAncestors((ConceptInterface) el, result);
              }
            });

    return result;
  }

  private void validateNamesAreUnique(
      List<? extends NamespacedEntity> elements, ValidationResult result) {
    Map<String, List<NamespacedEntity>> elementsByName =
        elements.stream()
            .filter(namespacedEntity -> namespacedEntity.getName() != null)
            .collect(Collectors.groupingBy((NamespacedEntity::getName)));
    elementsByName
        .entrySet()
        .forEach(
            (Map.Entry<String, List<NamespacedEntity>> entry) -> {
              if (entry.getValue().size() > 1) {
                entry
                    .getValue()
                    .forEach(
                        (NamespacedEntity el) ->
                            result.addError("Duplicate name " + el.getName(), el));
              }
            });
  }

  private void validateKeysAreNotNull(Metamodel metamodel, ValidationResult result) {
    metamodel
        .thisAndAllDescendants()
        .forEach(
            n -> {
              if (n instanceof HasKey<?>) {
                HasKey<?> hasKey = (HasKey<?>) n;
                String key = hasKey.getKey();
                if (key == null) {
                  result.addError("Key should not be null", n);
                }
              }
            });
  }

  private void validateKeysAreUnique(Metamodel metamodel, ValidationResult result) {
    Map<String, String> uniqueKeys = new HashMap<>();
    metamodel
        .thisAndAllDescendants()
        .forEach(
            n -> {
              if (n instanceof HasKey<?>) {
                HasKey<?> hasKey = (HasKey<?>) n;
                String key = hasKey.getKey();
                if (key != null) {
                  if (uniqueKeys.containsKey(key)) {
                    result.addError(
                        "Key " + key + " is duplicate. It is also used by " + uniqueKeys.get(key),
                        n);
                  } else {
                    uniqueKeys.put(key, n.getID());
                  }
                }
              }
            });
  }

  public boolean isMetamodelValid(Metamodel metamodel) {
    return validateMetamodel(metamodel).isSuccessful();
  }

  private void checkAncestors(Concept concept, ValidationResult validationResult) {
    checkAncestorsHelper(new HashSet<>(), concept, validationResult, true);
  }

  private void checkAncestors(
      ConceptInterface conceptInterface, ValidationResult validationResult) {
    checkAncestorsHelper(new HashSet<>(), conceptInterface, validationResult, false);
  }

  private void checkAncestorsHelper(
      Set<FeaturesContainer> alreadyExplored,
      Concept concept,
      ValidationResult validationResult,
      boolean examiningConcept) {
    if (alreadyExplored.contains(concept)) {
      validationResult.addError("Cyclic hierarchy found", concept);
    } else {
      alreadyExplored.add(concept);
      if (concept.getExtendedConcept() != null) {
        checkAncestorsHelper(
            alreadyExplored, concept.getExtendedConcept(), validationResult, examiningConcept);
      }
      concept
          .getImplemented()
          .forEach(
              interf ->
                  checkAncestorsHelper(
                      alreadyExplored, interf, validationResult, examiningConcept));
    }
  }

  private void checkAncestorsHelper(
      Set<FeaturesContainer> alreadyExplored,
      ConceptInterface conceptInterface,
      ValidationResult validationResult,
      boolean examiningConcept) {
    if (alreadyExplored.contains(conceptInterface)) {
      // It is ok to indirectly implement multiple time the same interface for a Concept.
      // It is instead an issue in case we are looking into interfaces.
      //
      // For example, this is fine:
      // class A extends B, implements I
      // class B implements I
      //
      // This is not fine:
      // interface I1 extends I2
      // interface I2 extends I1
      if (!examiningConcept) {
        validationResult.addError("Cyclic hierarchy found", conceptInterface);
      }
    } else {
      alreadyExplored.add(conceptInterface);
      conceptInterface
          .getExtendedInterfaces()
          .forEach(
              interf ->
                  checkAncestorsHelper(
                      alreadyExplored, interf, validationResult, examiningConcept));
    }
  }

  private void checkAncestorsHelperForConceptInterfaces(
      Set<ConceptInterface> alreadyExplored,
      ConceptInterface conceptInterface,
      ValidationResult validationResult) {
    if (alreadyExplored.contains(conceptInterface)) {
      validationResult.addError("Cyclic hierarchy found", conceptInterface);
    } else {
      alreadyExplored.add(conceptInterface);
      conceptInterface
          .getExtendedInterfaces()
          .forEach(
              interf ->
                  checkAncestorsHelperForConceptInterfaces(
                      alreadyExplored, interf, validationResult));
    }
  }

  public ValidationResult validateMetamodel(Metamodel metamodel) {
    ValidationResult result = new ValidationResult();

    result.checkForError(metamodel.getName() == null, "Qualified name not set", metamodel);

    validateNamesAreUnique(metamodel.getElements(), result);
    validateKeysAreNotNull(metamodel, result);
    validateKeysAreUnique(metamodel, result);

    // TODO once we implement the Node interface we could navigate the tree differently

    metamodel
        .getElements()
        .forEach(
            (MetamodelElement el) -> {
              result
                  .checkForError(el.getName() == null, "Simple name not set", el)
                  .checkForError(el.getMetamodel() == null, "Metamodel not set", el)
                  .checkForError(
                      el.getMetamodel() != null && el.getMetamodel() != metamodel,
                      "Metamodel not set correctly",
                      el);

              if (el instanceof io.lionweb.lioncore.java.metamodel.Enumeration) {
                io.lionweb.lioncore.java.metamodel.Enumeration enumeration = (Enumeration) el;
                enumeration
                    .getLiterals()
                    .forEach(
                        (EnumerationLiteral lit) ->
                            result.checkForError(
                                lit.getName() == null, "Simple name not set", lit));
                validateNamesAreUnique(enumeration.getLiterals(), result);
              }
              if (el instanceof FeaturesContainer) {
                FeaturesContainer<M3Node> featuresContainer = (FeaturesContainer) el;
                featuresContainer
                    .getFeatures()
                    .forEach(
                        (Feature feature) ->
                            result
                                .checkForError(
                                    feature.getName() == null, "Simple name not set", feature)
                                .checkForError(
                                    feature.getContainer() == null, "Container not set", feature)
                                .checkForError(
                                    feature.getContainer() != null
                                        && feature.getContainer() != featuresContainer,
                                    "Features container not set correctly",
                                    feature));
                validateNamesAreUnique(featuresContainer.getFeatures(), result);
              }
              if (el instanceof Concept) {
                Concept concept = (Concept) el;
                checkAncestors(concept, result);
                result.checkForError(
                    concept.getImplemented().size()
                        != concept.getImplemented().stream().distinct().count(),
                    "The same interface has been implemented multiple times",
                    concept);
              }
              if (el instanceof ConceptInterface) {
                checkAncestors((ConceptInterface) el, result);
              }
            });

    return result;
  }
}
