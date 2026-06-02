package io.lionweb.emf;

import org.jetbrains.annotations.NotNull;

/** EMF Resource type. */
public enum ResourceType {
  XML("xml"),
  JSON("json"),
  ECORE("ecore");
  private final String extension;

  ResourceType(@NotNull String extension) {
    this.extension = extension;
  }

  public @NotNull String getExtension() {
    return this.extension;
  }
}
