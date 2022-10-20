package org.lionweb.lioncore.java;

import java.util.LinkedList;
import java.util.List;

/**
 * This represents a group of elements that shares some characteristics.
 *
 * For example, Dated and Invoice could be both AbstractConcepts, while having different levels of tightness in the
 * groups.
 *
 * AbstractConcept is similar to {@link org.eclipse.emf.ecore.EClass EClass} in Ecore (which is used both for classes and interfaces)
 * and to
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESAbstractConcept">SAbstractConcept</a> /
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1169125787135">AbstractConceptDeclaration</a>
 * in MPS.
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
