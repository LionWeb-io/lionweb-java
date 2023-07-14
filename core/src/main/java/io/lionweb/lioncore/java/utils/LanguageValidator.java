package io.lionweb.lioncore.java.utils;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.language.Enumeration;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.M3Node;
import java.util.*;
import java.util.stream.Collectors;

public class LanguageValidator extends Validator<Language> {

  public static void ensureIsValid(Language language) {
    ValidationResult vr = new LanguageValidator().validate(language);
    if (!vr.isSuccessful()) {
      throw new RuntimeException("Invalid language: " + vr.getIssues());
    }
  }

  @Override
  public ValidationResult validate(Language language) {
    // Given languages are also valid node trees, we check against errors for node trees
    ValidationResult result = new NodeTreeValidator().validate(language);

    language
        .thisAndAllDescendants()
        .forEach(
            n ->
                result.checkForError(
                    !CommonChecks.isValidID(n.getID()),
                    "Node IDs should respect the format for IDs",
                    n));

    language
        .thisAndAllDescendants()
        .forEach(
            n -> {
              if (n instanceof IKeyed<?>) {
                IKeyed<?> hk = (IKeyed<?>) n;
                result.checkForError(
                    !CommonChecks.isValidID(hk.getKey()),
                    "Keys should respect the format for IDs",
                    n);
              }
            });

    result.checkForError(language.getName() == null, "Qualified name not set", language);

    validateNamesAreUnique(language.getElements(), result);

    // TODO once we implement the Node interface we could navigate the tree differently

    language
        .getElements()
        .forEach(
            (LanguageElement el) -> {
              result
                  .checkForError(el.getName() == null, "Simple name not set", el)
                  .checkForError(el.getLanguage() == null, "Language not set", el)
                  .checkForError(
                      el.getLanguage() != null && el.getLanguage() != language,
                      "Language not set correctly",
                      el);

              if (el instanceof io.lionweb.lioncore.java.language.Enumeration) {
                io.lionweb.lioncore.java.language.Enumeration enumeration =
                    (io.lionweb.lioncore.java.language.Enumeration) el;
                enumeration
                    .getLiterals()
                    .forEach(
                        (EnumerationLiteral lit) ->
                            result.checkForError(
                                lit.getName() == null, "Simple name not set", lit));
                validateNamesAreUnique(enumeration.getLiterals(), result);
              }
              if (el instanceof Classifier) {
                Classifier<M3Node> classifier = (Classifier) el;
                classifier
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
                                        && feature.getContainer() != classifier,
                                    "Features container not set correctly",
                                    feature));
                validateNamesAreUnique(classifier.getFeatures(), result);
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
                            result.addError("Duplicate name " + el.getName(), (Node) el));
              }
            });
  }

  private void validateKeysAreNotNull(Language language, ValidationResult result) {
    language
        .thisAndAllDescendants()
        .forEach(
            n -> {
              if (n instanceof IKeyed<?>) {
                IKeyed<?> IKeyed = (IKeyed<?>) n;
                String key = IKeyed.getKey();
                if (key == null) {
                  result.addError("Key should not be null", n);
                }
              }
            });
  }

  private void validateKeysAreUnique(Language language, ValidationResult result) {
    Map<String, String> uniqueKeys = new HashMap<>();
    language
        .thisAndAllDescendants()
        .forEach(
            n -> {
              if (n instanceof IKeyed<?>) {
                IKeyed<?> IKeyed = (IKeyed<?>) n;
                String key = IKeyed.getKey();
                if (key != null) {
                  if (uniqueKeys.containsKey(key)) {
                    result.addError(
                        "Key '" + key + "' is duplicate. It is also used by " + uniqueKeys.get(key),
                        n);
                  } else {
                    uniqueKeys.put(key, n.getID());
                  }
                }
              }
            });
  }

  public boolean isLanguageValid(Language language) {
    return validateLanguage(language).isSuccessful();
  }

  private void checkAncestors(Concept concept, ValidationResult validationResult) {
    checkAncestorsHelper(new HashSet<>(), concept, validationResult, true);
  }

  private void checkAncestors(
      ConceptInterface conceptInterface, ValidationResult validationResult) {
    checkAncestorsHelper(new HashSet<>(), conceptInterface, validationResult, false);
  }

  private void checkAncestorsHelper(
      Set<Classifier> alreadyExplored,
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
      Set<Classifier> alreadyExplored,
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

  public ValidationResult validateLanguage(Language language) {
    ValidationResult result = new ValidationResult();

    result.checkForError(language.getName() == null, "Qualified name not set", language);

    validateNamesAreUnique(language.getElements(), result);
    validateKeysAreNotNull(language, result);
    validateKeysAreUnique(language, result);

    // TODO once we implement the Node interface we could navigate the tree differently

    language
        .getElements()
        .forEach(
            (LanguageElement el) -> {
              result
                  .checkForError(el.getName() == null, "Simple name not set", el)
                  .checkForError(el.getLanguage() == null, "Language not set", el)
                  .checkForError(
                      el.getLanguage() != null && el.getLanguage() != language,
                      "Language not set correctly",
                      el);

              if (el instanceof io.lionweb.lioncore.java.language.Enumeration) {
                io.lionweb.lioncore.java.language.Enumeration enumeration = (Enumeration) el;
                enumeration
                    .getLiterals()
                    .forEach(
                        (EnumerationLiteral lit) ->
                            result.checkForError(
                                lit.getName() == null, "Simple name not set", lit));
                validateNamesAreUnique(enumeration.getLiterals(), result);
              }
              if (el instanceof Classifier) {
                Classifier<M3Node> classifier = (Classifier) el;
                classifier
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
                                        && feature.getContainer() != classifier,
                                    "Features container not set correctly",
                                    feature));
                validateNamesAreUnique(classifier.getFeatures(), result);
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
