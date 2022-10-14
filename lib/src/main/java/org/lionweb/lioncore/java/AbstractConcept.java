package org.lionweb.lioncore.java;

import java.util.LinkedList;
import java.util.List;

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
