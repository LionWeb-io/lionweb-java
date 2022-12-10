package org.lionweb.lioncore.java.utils;

import java.util.List;
import java.util.function.Supplier;

public interface Validatable {

    class ValidationResult {
        private List<Issue> issues;

        public List<Issue> getIssues() {
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

    ValidationResult validate();
}
