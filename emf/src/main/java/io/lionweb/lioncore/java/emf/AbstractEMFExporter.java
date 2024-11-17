package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import java.util.Objects;
import javax.annotation.Nonnull;

public abstract class AbstractEMFExporter {
  protected final ConceptsToEClassesMapping conceptsToEClassesMapping;
  private LionWebVersion lionWebVersion;

  protected AbstractEMFExporter() {
    this(LionWebVersion.currentVersion);
  }

  protected AbstractEMFExporter(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    this.lionWebVersion = lionWebVersion;
    this.conceptsToEClassesMapping = new ConceptsToEClassesMapping();
  }

  public AbstractEMFExporter(ConceptsToEClassesMapping conceptsToEClassesMapping) {
    this.conceptsToEClassesMapping = conceptsToEClassesMapping;
  }

  public LionWebVersion getLionWebVersion() {
    return lionWebVersion;
  }
}
