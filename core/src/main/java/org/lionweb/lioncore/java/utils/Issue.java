package org.lionweb.lioncore.java.utils;

import java.util.Objects;

public class Issue {
    private String message;
    private IssueSeverity severity;

    public Issue(IssueSeverity severity, String message) {
        this.message = message;
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public IssueSeverity getSeverity() {
        return severity;
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
        return Objects.equals(message, issue.message) && severity == issue.severity;
    }


    @Override
    public String toString() {
        return "Issue{" +
                "message='" + message + '\'' +
                ", severity=" + severity +
                '}';
    }


    public boolean isError() {
        return severity == IssueSeverity.Error;
    }
}
