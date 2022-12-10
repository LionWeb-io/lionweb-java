package org.lionweb.lioncore.java.utils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * An element that can self-check its status. This is typically useful when we want to allow elements to be in a
 * temporary invalid state. For example, this is typically the case during unserialization or as an intermediate
 * step in a larger transaction. Eventually we want to validate the element for consistency, and we can do that
 * through the methods provided by this interface.
 */
public interface Validatable {

    class ValidationResult {
        private Set<Issue> issues = new HashSet<>();

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

    /**
     * This typically return exclusively error on the element itself on not the errors in all the descendants
     * of this element.
     */
    ValidationResult validate();

    default boolean isValid() {
        return validate().isSuccessful();
    }
}
