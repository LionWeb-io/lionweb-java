package io.lionweb.lioncore.java.model;

import io.lionweb.lioncore.java.Experimental;
import io.lionweb.lioncore.java.metamodel.Language;
import java.util.List;

/**
 * This represents a container of nodes.
 *
 * @see org.eclipse.emf.ecore.resource.Resource Ecore equivalent <i>Resource</i>
 * @see <a href="https://www.jetbrains.com/help/mps/mps-project-structure.html#models">MPS
 *     equivalent <i>Models</i> in documentation</a>
 *     <p>TODO consider if the Model should have a version too
 */
@Experimental
public interface Model {
  /** Return the fully qualified name associated with the model. */
  String getName();

  void setName(String name);

  /**
   * Return the list of models imported by this model. All nodes contained in the Model could refer
   * exclusively to nodes part of this model or nodes contained in imported models.
   *
   * <p>TODO: consider versioning of models
   */
  List<Model> getImportedModels();

  /**
   * Return the list of metamodels used by this model. All nodes contained in the Model could use
   * concepts defined in the listed metamodels.
   *
   * <p>TODO: consider versioning of metamodels
   */
  List<Language> getUsedMetamodels();

  /** Return the list of top level nodes contained in the model. */
  List<Node> getRoots();
}
