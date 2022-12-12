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

        public ValidationResult checkForError(Supplier<Boolean> check, String message, Object subject) {
            if (check.get()) {
                issues.add(new Issue(IssueSeverity.Error, message, subject));
            }
            return this;
        }

        public ValidationResult addError(String message, Object subject) {
            issues.add(new Issue(IssueSeverity.Error, message, subject));
            return this;
        }

        public <S> ValidationResult checkForError(Function<S, Boolean> check, String message, S subject) {
            if (check.apply(subject)) {
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

    public ValidationResult validateMetamodel(Metamodel metamodel) {
        ValidationResult result = new ValidationResult();

        result.checkForError((Metamodel sub) -> sub.getQualifiedName() == null,
                "Qualified name not set", metamodel);

        validateNamesAreUnique(metamodel.getElements(), result);

        metamodel.getElements().forEach((MetamodelElement el) -> {
            result
                    .checkForError((MetamodelElement sub) -> sub.getSimpleName() == null, "Simple name not set", el)
                    .checkForError((MetamodelElement sub) -> sub.getMetamodel() == null, "Metamodel not set", el)
                    .checkForError((MetamodelElement sub) -> sub.getMetamodel() != null && sub.getMetamodel() != metamodel,
                            "Metamodel not set correctly", el);

            if (el instanceof Enumeration) {
                Enumeration enumeration = (Enumeration) el;
                enumeration.getLiterals().forEach((EnumerationLiteral lit) ->
                        result.checkForError((EnumerationLiteral sub) -> sub.getSimpleName() == null, "Simple name not set", lit));
                validateNamesAreUnique(enumeration.getLiterals(), result);
            }
            if (el instanceof FeaturesContainer) {
                FeaturesContainer featuresContainer = (FeaturesContainer) el;
                featuresContainer.getFeatures().forEach((Feature feature)->
                        result
                                .checkForError((Feature sub) -> sub.getSimpleName() == null, "Simple name not set", feature)
                                .checkForError((Feature sub) -> sub.getContainer() == null, "Container not set", feature));
                validateNamesAreUnique(featuresContainer.getFeatures(), result);
            }
        });

        return result;
    }
}
