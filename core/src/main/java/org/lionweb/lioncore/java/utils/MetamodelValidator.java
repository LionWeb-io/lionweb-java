package org.lionweb.lioncore.java.utils;

import org.lionweb.lioncore.java.metamodel.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MetamodelValidator {

    final class ValidationResult {
        private final Set<Issue> issues = new HashSet<>();

        public Set<Issue> getIssues() {
            return issues;
        }

        public boolean isSuccessful() {
            return issues.stream().noneMatch(issue -> issue.getSeverity() == IssueSeverity.Error);
        }

        public ValidationResult addError(String message, Object subject) {
            issues.add(new Issue(IssueSeverity.Error, message, subject));
            return this;
        }

        public <S> ValidationResult checkForError(boolean check, String message, S subject) {
            if (check) {
                issues.add(new Issue(IssueSeverity.Error, message, subject));
            }
            return this;
        }
    }

    private void validateNamesAreUnique(List<? extends NamespacedEntity> elements, ValidationResult result) {
        Map<String, List<NamespacedEntity>> elementsByName = elements.stream()
                .filter(namespacedEntity -> namespacedEntity.getSimpleName() != null)
                .collect(Collectors.groupingBy((NamespacedEntity::getSimpleName)));
        elementsByName.entrySet().forEach((Map.Entry<String, List<NamespacedEntity>> entry)-> {
            if (entry.getValue().size() > 1) {
                entry.getValue().forEach((NamespacedEntity el) -> result.addError("Duplicate name", el));
            }
        });
    }

    public boolean isMetamodelValid(Metamodel metamodel) {
        return validateMetamodel(metamodel).isSuccessful();
    }

    private void checkAncestors(Concept concept, ValidationResult validationResult) {
        checkAncestorsHelper(new HashSet<>(), concept, validationResult, true);
    }

    private void checkAncestors(ConceptInterface conceptInterface, ValidationResult validationResult) {
        checkAncestorsHelper(new HashSet<>(), conceptInterface, validationResult, false);
    }

    private void checkAncestorsHelper(Set<FeaturesContainer> alreadyExplored, Concept concept,
                                                 ValidationResult validationResult, boolean examiningConcept) {
        if (alreadyExplored.contains(concept)) {
            validationResult.addError("Cyclic hierarchy found", concept);
        } else {
            alreadyExplored.add(concept);
            if (concept.getExtendedConcept() != null) {
                checkAncestorsHelper(alreadyExplored, concept.getExtendedConcept(), validationResult, examiningConcept);
            }
            concept.getImplemented().forEach(interf -> checkAncestorsHelper(alreadyExplored, interf, validationResult, examiningConcept));
        }
    }

    private void checkAncestorsHelper(Set<FeaturesContainer> alreadyExplored, ConceptInterface conceptInterface,
                                      ValidationResult validationResult, boolean examiningConcept) {
        if (alreadyExplored.contains(conceptInterface)) {
            // It is ok to implement multiple time the same interface, we just should avoid a stack overflow
            // It is instead an issue in case we are looking into interfaces
            if (!examiningConcept) {
                validationResult.addError("Cyclic hierarchy found", conceptInterface);
            }
        } else {
            alreadyExplored.add(conceptInterface);
            conceptInterface.getExtendedInterface().forEach(interf -> checkAncestorsHelper(alreadyExplored, interf, validationResult, examiningConcept));
        }
    }

    private void checkAncestorsHelperForConceptInterfaces(Set<ConceptInterface> alreadyExplored, ConceptInterface conceptInterface, ValidationResult validationResult) {
        if (alreadyExplored.contains(conceptInterface)) {
            validationResult.addError("Cyclic hierarchy found", conceptInterface);
        } else {
            alreadyExplored.add(conceptInterface);
            conceptInterface.getExtendedInterface().forEach(interf -> checkAncestorsHelperForConceptInterfaces(alreadyExplored, interf, validationResult));
        }
    }

    public ValidationResult validateMetamodel(Metamodel metamodel) {
        ValidationResult result = new ValidationResult();

        result.checkForError(metamodel.getQualifiedName() == null,
                "Qualified name not set", metamodel);

        validateNamesAreUnique(metamodel.getElements(), result);

        // TODO once we implement the Node interface we could navigate the tree differently

        metamodel.getElements().forEach((MetamodelElement el) -> {
            result
                    .checkForError(el.getSimpleName() == null, "Simple name not set", el)
                    .checkForError(el.getMetamodel() == null, "Metamodel not set", el)
                    .checkForError(el.getMetamodel() != null && el.getMetamodel() != metamodel,
                            "Metamodel not set correctly", el);

            if (el instanceof Enumeration) {
                Enumeration enumeration = (Enumeration) el;
                enumeration.getLiterals().forEach((EnumerationLiteral lit) ->
                        result.checkForError(lit.getSimpleName() == null, "Simple name not set", lit));
                validateNamesAreUnique(enumeration.getLiterals(), result);
            }
            if (el instanceof FeaturesContainer) {
                FeaturesContainer featuresContainer = (FeaturesContainer) el;
                featuresContainer.getFeatures().forEach((Feature feature)->
                        result
                                .checkForError(feature.getSimpleName() == null, "Simple name not set", feature)
                                .checkForError(feature == null, "Container not set", feature)
                                .checkForError(feature != null && feature.getContainer() != featuresContainer,
                                        "Features container not set correctly", feature));
                validateNamesAreUnique(featuresContainer.getFeatures(), result);
            }
            if (el instanceof Concept) {
                checkAncestors((Concept) el, result);
            }
            if (el instanceof ConceptInterface) {
                checkAncestors((ConceptInterface) el, result);
            }
        });

        return result;
    }
}
