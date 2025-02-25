package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.emf.mapping.LanguageEntitiesToEElementsMapping;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * Importer that given an EMF Resource imports something out of it.
 *
 * @param <E> kind of imported element
 */
public abstract class AbstractEMFImporter<E> {
  protected final LanguageEntitiesToEElementsMapping entitiesToEElementsMapping;
  private LionWebVersion lionWebVersion;

  public AbstractEMFImporter() {
    this(LionWebVersion.currentVersion);
  }

  public AbstractEMFImporter(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    this.lionWebVersion = lionWebVersion;
    this.entitiesToEElementsMapping = new LanguageEntitiesToEElementsMapping(lionWebVersion);
  }

  /**
   * Not that in this case the LionWeb Version used will be "embedded" in the
   * ConceptsToEClassesMapping instance.
   */
  public AbstractEMFImporter(LanguageEntitiesToEElementsMapping entitiesToEElementsMapping) {
    this.entitiesToEElementsMapping = entitiesToEElementsMapping;
    this.lionWebVersion = entitiesToEElementsMapping.getLionWebVersion();
  }

  public abstract List<E> importResource(Resource resource);

  public @Nonnull LionWebVersion getLionWebVersion() {
    return lionWebVersion;
  }
}
