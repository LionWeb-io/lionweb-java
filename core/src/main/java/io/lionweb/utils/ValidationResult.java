package io.lionweb.utils;

import io.lionweb.model.ClassifierInstance;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ValidationResult {
  private final Set<Issue> issues = new HashSet<>();

  public Set<Issue> getIssues() {
    return issues;
  }

  public boolean isSuccessful() {
    return issues.stream().noneMatch(issue -> issue.getSeverity() == IssueSeverity.Error);
  }

  public ValidationResult addError(
      @Nonnull String message, @Nullable ClassifierInstance<?> subject) {
    Objects.requireNonNull(message, "message should not be null");
    issues.add(new Issue(IssueSeverity.Error, message, subject));
    return this;
  }

  public ValidationResult addError(@Nonnull String message, @Nullable String subject) {
    Objects.requireNonNull(message, "message should not be null");
    issues.add(new Issue(IssueSeverity.Error, message, subject));
    return this;
  }

  public ValidationResult addError(@Nonnull String message) {
    return addError(message, (String) null);
  }

  /**
   * Checks the specified condition and, if the condition evaluates to true, adds an error to the
   * current validation result with the provided message and subject.
   *
   * @param check a boolean condition that, when true, will trigger an error to be added
   * @param message a non-null string describing the error; must not be null
   * @param subject an optional subject associated with the error, which can be null.
   * @return the current ValidationResult, potentially updated with a new error if the condition was
   *     true
   * @throws NullPointerException if the message is null
   */
  public <S extends ClassifierInstance<?>> ValidationResult addErrorIf(
      boolean check, @Nonnull String message, @Nullable S subject) {
    Objects.requireNonNull(message, "message should not be null");
    if (check) {
      issues.add(new Issue(IssueSeverity.Error, message, subject));
    }
    return this;
  }

  /**
   * Checks the specified condition and, if the condition evaluates to true, adds an error to the
   * {@link ValidationResult} with the provided message and subject.
   *
   * @param check a boolean condition that, when true, will trigger an error to be added
   * @param message a non-null String message describing the error; must not be null
   * @param subject an optional subject associated with the error, which can be null. This subject
   *     is the id of the node or annotation that caused the error.
   * @return the current {@link ValidationResult}, potentially updated with a new error if the
   *     condition was true
   * @throws NullPointerException if the message is null
   */
  public ValidationResult addErrorIf(
      boolean check, @Nonnull String message, @Nullable String subject) {
    Objects.requireNonNull(message, "message should not be null");
    if (check) {
      issues.add(new Issue(IssueSeverity.Error, message, subject));
    }
    return this;
  }

  /**
   * Checks the specified condition and, if the condition evaluates to true, adds an error to the
   * {@link ValidationResult} with the provided message. This message will have no subject.
   *
   * @param check a boolean condition that, when true, will trigger an error to be added
   * @param message a non-null String message describing the error; must not be null
   * @return the current {@link ValidationResult}, potentially updated with a new error if the
   *     condition was true
   * @throws NullPointerException if the message is null
   */
  public ValidationResult addErrorIf(boolean check, @Nonnull String message) {
    return addErrorIf(check, message, (String) null);
  }

  public ValidationResult addErrorIf(
      boolean check,
      @Nonnull Supplier<String> messageSupplier,
      @Nullable ClassifierInstance<?> subject) {
    if (check) {
      return addErrorIf(check, messageSupplier.get(), subject);
    }
    return this;
  }

  public ValidationResult addErrorIf(boolean check, @Nonnull Supplier<String> messageSupplier) {
    return addErrorIf(check, messageSupplier, null);
  }

  @Override
  public String toString() {
    return "ValidationResult("
        + issues.stream().map(Issue::toString).collect(Collectors.joining(", "))
        + ")";
  }
}
