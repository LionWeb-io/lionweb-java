package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.emf.mapping.LanguageEntitiesToEElementsMapping;
import java.util.Objects;
import javax.annotation.Nonnull;

public abstract class AbstractEMFExporter {
  protected final LanguageEntitiesToEElementsMapping entitiesToEElementsMapping;
  private LionWebVersion lionWebVersion;

  protected AbstractEMFExporter() {
    this(LionWebVersion.currentVersion);
  }

  protected AbstractEMFExporter(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    this.lionWebVersion = lionWebVersion;
    this.entitiesToEElementsMapping = new LanguageEntitiesToEElementsMapping(lionWebVersion);
  }

  public AbstractEMFExporter(LanguageEntitiesToEElementsMapping entitiesToEElementsMapping) {
    this.entitiesToEElementsMapping = entitiesToEElementsMapping;
  }

  public @Nonnull LionWebVersion getLionWebVersion() {
    return lionWebVersion;
  }
}
