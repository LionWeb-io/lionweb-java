package io.lionweb.utils;

import io.lionweb.model.Node;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationResult {
  private final Set<Issue> issues = new HashSet<>();

  public Set<Issue> getIssues() {
    return issues;
  }

  public boolean isSuccessful() {
    return issues.stream().noneMatch(issue -> issue.getSeverity() == IssueSeverity.Error);
  }

  public ValidationResult addError(String message, Node subject) {
    issues.add(new Issue(IssueSeverity.Error, message, subject));
    return this;
  }

  public <S extends Node> ValidationResult checkForError(boolean check, String message, S subject) {
    if (check) {
      issues.add(new Issue(IssueSeverity.Error, message, subject));
    }
    return this;
  }

  @Override
  public String toString() {
    return "ValidationResult("
        + issues.stream().map(Issue::toString).collect(Collectors.joining(", "))
        + ")";
  }
}
