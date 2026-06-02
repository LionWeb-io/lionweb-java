package io.lionweb.emf;

import io.lionweb.LionWebVersion;
import io.lionweb.emf.mapping.LanguageEntitiesToEElementsMapping;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Base class for EMF exporters, providing shared configuration (LionWeb version and
 * entity-to-EElement mapping).
 */
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
    this.lionWebVersion = entitiesToEElementsMapping.getLionWebVersion();
  }

  public @Nonnull LionWebVersion getLionWebVersion() {
    return lionWebVersion;
  }
}
