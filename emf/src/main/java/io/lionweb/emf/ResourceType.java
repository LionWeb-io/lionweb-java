package io.lionweb.emf;

/** EMF Resource type. */
public enum ResourceType {
  XML("xml"),
  JSON("json"),
  ECORE("ecore");
  private final String extension;

  ResourceType(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return this.extension;
  }
}
