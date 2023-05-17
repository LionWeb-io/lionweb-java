package io.lionweb.lioncore.java.emf;

/** EMF Resource type. */
public enum ResourceType {
  XML("xml"),
  JSON("json"),
  ECORE("ecore");
  private String extension;

  ResourceType(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return this.extension;
  }
}
