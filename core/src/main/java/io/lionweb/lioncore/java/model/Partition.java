package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.Experimental;
import io.lionweb.lioncore.java.language.Language;
import java.util.List;

/**
 * This represents a container of nodes. This idea was not retained. Partitions are now simple
 * nodes, and this should be removed.
 *
 * @see org.eclipse.emf.ecore.resource.Resource Ecore equivalent <i>Resource</i>
 * @see <a href="https://www.jetbrains.com/help/mps/mps-project-structure.html#models">MPS
 *     equivalent <i>Models</i> in documentation</a>
 *     <p>TODO consider if the Model should have a version too
 */
@Experimental
@Deprecated
public interface Partition {
  /** Return the fully qualified name associated with the model. */
  String getName();

  void setName(String name);

  /**
   * Return the list of models imported by this model. All nodes contained in the Model could refer
   * exclusively to nodes part of this model or nodes contained in imported models.
   *
   * <p>TODO: consider versioning of models
   */
  List<Partition> getImportedModels();

  /**
   * Return the list of languages used by this model. All nodes contained in the Model could use
   * concepts defined in the listed languages.
   *
   * <p>TODO: consider versioning of languages
   */
  List<Language> getUsedLanguages();

  /** Return the list of top level nodes contained in the model. */
  List<Node> getRoots();
}
