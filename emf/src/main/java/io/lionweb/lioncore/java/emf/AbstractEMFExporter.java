package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;

public abstract class AbstractEMFExporter {
  protected final ConceptsToEClassesMapping conceptsToEClassesMapping;

  protected AbstractEMFExporter() {
    this.conceptsToEClassesMapping = new ConceptsToEClassesMapping();
  }

  public AbstractEMFExporter(ConceptsToEClassesMapping conceptsToEClassesMapping) {
    this.conceptsToEClassesMapping = conceptsToEClassesMapping;
  }
}
