package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
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

  protected final ConceptsToEClassesMapping conceptsToEClassesMapping;

  public AbstractEMFImporter() {
    this(LionWebVersion.currentVersion);
  }

  public AbstractEMFImporter(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    this.conceptsToEClassesMapping = new ConceptsToEClassesMapping(lionWebVersion);
  }

  /**
   * Not that in this case the LionWeb Version used will be "embedded" in the
   * ConceptsToEClassesMapping instance.
   */
  public AbstractEMFImporter(ConceptsToEClassesMapping conceptsToEClassesMapping) {
    this.conceptsToEClassesMapping = conceptsToEClassesMapping;
  }

  public abstract List<E> importResource(Resource resource);
}
