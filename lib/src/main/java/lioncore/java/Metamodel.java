package lioncore.java;

import java.util.List;

public class Metamodel implements NamespaceProvider {
    private String qualifiedName;
    public List<Metamodel> dependsOn() {
        throw new UnsupportedOperationException();
    }
    public List<MetamodelElement> getElements() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String namespaceQualifier() {
        return qualifiedName;
    }
}
