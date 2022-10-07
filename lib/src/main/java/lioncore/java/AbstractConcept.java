package lioncore.java;

import java.util.List;

public abstract class AbstractConcept extends MetamodelElement implements NamespaceProvider, FeaturesContainer {
    public List<Feature> allFeatures() {
        throw new UnsupportedOperationException();
    }
}
