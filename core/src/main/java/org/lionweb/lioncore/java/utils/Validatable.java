package org.lionweb.lioncore.java.utils;

import java.util.List;

public interface Validatable {

    class ValidationResult {
        private List<Issue> issues;

        public List<Issue> getIssues() {
            return issues;
        }

        public boolean isSuccessful() {
            return issues.stream().noneMatch(issue -> issue.getSeverity() == IssueSeverity.Error);
        }
    }

    ValidationResult validate();
}
