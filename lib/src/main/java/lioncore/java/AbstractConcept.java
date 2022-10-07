package lioncore.java;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractConcept extends MetamodelElement implements NamespaceProvider, FeaturesContainer {
    protected List<Feature> features = new LinkedList<>();

    public AbstractConcept(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<Feature> allFeatures() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Feature> getFeatures() {
        return this.features;
    }

    @Override
    public String namespaceQualifier() {
        return this.qualifiedName();
    }
}
