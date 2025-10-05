package io.lionweb.utils;

import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.impl.ProxyNode;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Issue {
  private final String message;
  private final IssueSeverity severity;

  private final @Nullable ClassifierInstance<?> subject;

  public Issue(
      @Nonnull IssueSeverity severity,
      @Nonnull String message,
      @Nullable ClassifierInstance<?> subject) {
    Objects.requireNonNull(severity, "severity should not be null");
    Objects.requireNonNull(message, "message should not be null");
    this.message = message;
    this.severity = severity;
    this.subject = subject;
  }

  public Issue(@Nonnull IssueSeverity severity, @Nonnull String message, @Nullable String nodeID) {
    this(severity, message, nodeID == null ? null : new ProxyNode(nodeID));
  }

  public String getMessage() {
    return message;
  }

  public IssueSeverity getSeverity() {
    return severity;
  }

  public @Nullable ClassifierInstance<?> getSubject() {
    return subject;
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, severity);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Issue issue = (Issue) o;
    return Objects.equals(message, issue.message)
        && severity == issue.severity
        && subject == issue.subject;
  }

  @Override
  public String toString() {
    return "Issue{"
        + "message='"
        + message
        + '\''
        + ", severity="
        + severity
        + ", subject="
        + subject
        + '}';
  }

  public boolean isError() {
    return severity == IssueSeverity.Error;
  }
}
