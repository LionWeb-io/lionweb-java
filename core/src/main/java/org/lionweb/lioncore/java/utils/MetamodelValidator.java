package org.lionweb.lioncore.java.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class MetamodelValidator {

    final class ValidationResult {
        private final Set<Issue> issues = new HashSet<>();

        public Set<Issue> getIssues() {
            return issues;
        }

        public boolean isSuccessful() {
            return issues.stream().noneMatch(issue -> issue.getSeverity() == IssueSeverity.Error);
        }

        public ValidationResult checkForError(Supplier<Boolean> check, String message) {
            if (check.get()) {
                issues.add(new Issue(IssueSeverity.Error, message));
            }
            return this;
        }
    }

    public ValidationResult validateMetamodel() {
        // EnumerationLiteral
        new Validatable.ValidationResult()
                .checkForError(() -> getName() == null, "Name not set");

        // Feature
        .checkForError(() -> getSimpleName() == null, "Simple name not set")
        .checkForError(() -> getContainer() == null, "Container not set");

        // Metamodel
        .checkForError(() -> getQualifiedName() == null, "Qualified name not set")

        // Metamodel element
                .checkForError(() -> getSimpleName() == null, "Simple name not set")
                .checkForError(() -> getMetamodel() == null, "Metamodel not set");
    }
}
