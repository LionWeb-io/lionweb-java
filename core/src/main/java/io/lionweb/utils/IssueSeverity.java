package io.lionweb.utils;

/**
 * Enum representing the severity of an issue.
 *
 * <ul>
 *   <li>{@code Warning}: Indicates a non-critical issue that may require attention but does not
 *       prevent the process from continuing.
 *   <li>{@code Error}: Indicates a critical issue that prevents the process from completing
 *       successfully.
 * </ul>
 */
public enum IssueSeverity {
  Warning,
  Error
}
