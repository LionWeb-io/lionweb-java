package io.lionweb.lioncore.java.utils;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.language.Enumeration;
import io.lionweb.lioncore.java.model.Node;
import java.util.*;
import java.util.stream.Collectors;

public class LanguageValidator extends Validator<Language> {

  public static void ensureIsValid(Language language) {
    ValidationResult vr = new LanguageValidator().validate(language);
    if (!vr.isSuccessful()) {
      throw new RuntimeException("Invalid language: " + vr.getIssues());
    }
  }

  private void validateEnumeration(ValidationResult result, Enumeration enumeration) {
    enumeration
        .getLiterals()
        .forEach(
            (EnumerationLiteral lit) ->
                result.checkForError(lit.getName() == null, "Simple name not set", lit));
    validateNamesAreUnique(enumeration.getLiterals(), result);
  }

  private void validateClassifier(ValidationResult result, Classifier<?> classifier) {
    classifier
        .getFeatures()
        .forEach(
            (Feature feature) ->
                result
                    .checkForError(feature.getName() == null, "Simple name not set", feature)
                    .checkForError(feature.getContainer() == null, "Container not set", feature)
                    .checkForError(
                        feature.getContainer() != null
                            && ((Node) feature.getContainer()).getID() != null
                            && !((Node) feature.getContainer()).getID().equals(classifier.getID()),
                        "Features container not set correctly: set to "
                            + feature.getContainer()
                            + " when "
                            + classifier
                            + " was expected",
                        feature));
    validateNamesAreUnique(classifier.getFeatures(), result);
  }

  private void validateConcept(ValidationResult result, Concept concept) {
    checkAncestors(concept, result);
    result.checkForError(
        concept.getImplemented().size() != concept.getImplemented().stream().distinct().count(),
        "The same interface has been implemented multiple times",
        concept);
  }

  /** It checks if there is any circularity (direct or indirect) in the given StructuredDataType. */
  public static boolean isCircular(StructuredDataType structuredDataType) {
    Set<StructuredDataType> circularSDTs = new HashSet<>();
    Set<StructuredDataType> visited = new HashSet<>();
    Stack<StructuredDataType> toVisit = new Stack<>();
    toVisit.add(structuredDataType);

    while (!toVisit.isEmpty()) {
      StructuredDataType sdt = toVisit.pop();
      visited.add(sdt);
      sdt.getFields()
          .forEach(
              field -> {
                if (field.getType() instanceof StructuredDataType) {
                  StructuredDataType newSDT = (StructuredDataType) field.getType();
                  if (visited.contains(newSDT)) {
                    circularSDTs.add(newSDT);
                  } else {
                    toVisit.add((StructuredDataType) field.getType());
                  }
                }
              });
    }
    return !circularSDTs.isEmpty();
  }

  private void validateStructuralDataType(
      ValidationResult result, StructuredDataType structuredDataType) {
    if (isCircular(structuredDataType)) {
      result.addError(
          "Circular references are forbidden in StructuralDataFields", structuredDataType);
    }
  }

  @Override
  public ValidationResult validate(Language language) {
    // Given languages are also valid node trees, we check against errors for node trees
    ValidationResult result = new NodeTreeValidator().validate(language);

    result.checkForError(language.getName() == null, "Qualified name not set", language);

    validateNamesAreUnique(language.getElements(), result);
    validateKeysAreNotNull(language, result);
    validateKeysAreUnique(language, result);

    // TODO once we implement the Node interface we could navigate the tree differently

    language
        .getElements()
        .forEach(
            (LanguageEntity el) -> {
              result
                  .checkForError(el.getName() == null, "Simple name not set", el)
                  .checkForError(el.getLanguage() == null, "Language not set", el)
                  .checkForError(
                      el.getLanguage() != null && el.getLanguage() != language,
                      "Language not set correctly",
                      el);

              if (el instanceof io.lionweb.lioncore.java.language.Enumeration) {
                validateEnumeration(result, (Enumeration) el);
              }
              if (el instanceof Classifier) {
                validateClassifier(result, (Classifier) el);
              }
              if (el instanceof Concept) {
                validateConcept(result, (Concept) el);
              }
              if (el instanceof Interface) {
                checkInterfacesCycles((Interface) el, result);
              }
              if (el instanceof Annotation) {
                checkAnnotates((Annotation) el, result);
                checkAnnotationFeatures((Annotation) el, result);
              }
              if (el instanceof StructuredDataType) {
                validateStructuralDataType(result, (StructuredDataType) el);
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
    return validate(language).isSuccessful();
  }

  private void checkAncestors(Concept concept, ValidationResult validationResult) {
    checkAncestorsHelper(new HashSet<>(), concept, validationResult, true);
  }

  private void checkAncestors(Interface iface, ValidationResult validationResult) {
    checkAncestorsHelper(new HashSet<>(), iface, validationResult, false);
  }

  private void checkAnnotates(Annotation annotation, ValidationResult validationResult) {
    validationResult.checkForError(
        annotation.getEffectivelyAnnotated() == null,
        "An annotation should specify annotates or inherit it",
        annotation);
    validationResult.checkForError(
        annotation.getExtendedAnnotation() != null
            && annotation.getAnnotates() != null
            && annotation.getAnnotates() != annotation.getExtendedAnnotation().getAnnotates(),
        "When a sub annotation specify a value for annotates it must be the same value the super annotation specifies",
        annotation);
  }

  private void checkAnnotationFeatures(Annotation annotation, ValidationResult validationResult) {
    validationResult.checkForError(
        !annotation.allContainments().isEmpty(),
        "An annotation should not have containment links",
        annotation);
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
      Interface iface,
      ValidationResult validationResult,
      boolean examiningConcept) {
    if (alreadyExplored.contains(iface)) {
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
        validationResult.addError("Cyclic hierarchy found", iface);
      }
    } else {
      alreadyExplored.add(iface);
      iface
          .getExtendedInterfaces()
          .forEach(
              interf ->
                  checkAncestorsHelper(
                      alreadyExplored, interf, validationResult, examiningConcept));
    }
  }

  private void checkInterfacesCycles(Interface iface, ValidationResult validationResult) {
    if (iface.allExtendedInterfaces().contains(iface)) {
      validationResult.addError("Cyclic hierarchy found: the interface extends itself", iface);
    }
  }

  private void checkAncestorsHelperForInterfaces(
      Set<Interface> alreadyExplored, Interface iface, ValidationResult validationResult) {
    if (alreadyExplored.contains(iface)) {
      validationResult.addError("Cyclic hierarchy found", iface);
    } else {
      alreadyExplored.add(iface);
      iface
          .getExtendedInterfaces()
          .forEach(
              interf ->
                  checkAncestorsHelperForInterfaces(alreadyExplored, interf, validationResult));
    }
  }
}
