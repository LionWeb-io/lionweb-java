package org.lionweb.lioncore.java;

import java.util.LinkedList;
import java.util.List;

/**
 * This represents a group of elements that shares some characteristics.
 *
 * For example, Dated and Invoice could be both AbstractConcepts, while having different levels of tightness in the
 * groups.
 *
 * @see org.eclipse.emf.ecore.EClass Ecore equivalent <i>EClass</i> (which is used both for classes and interfaces)
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1169125787135">MPS equivalent <i>AbstractConceptDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SAbstractConcept MPS equivalent <i>SAbstractConcept</i> in SModel
 */
public abstract class AbstractConcept extends MetamodelElement implements NamespaceProvider, FeaturesContainer {
    private List<Feature> features = new LinkedList<>();

    public AbstractConcept(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<Feature> allFeatures() {
        // TODO Should this return features which are overriden?
        // TODO Should features be returned in a particular order?
        throw new UnsupportedOperationException();
    }

    // TODO should this expose an immutable list to force users to use methods on this class
    //      to modify the collection?
    @Override
    public List<Feature> getFeatures() {
        return this.features;
    }

    @Override
    public String namespaceQualifier() {
        return this.qualifiedName();
    }
}
