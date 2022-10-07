package lioncore.java;

import java.util.LinkedList;
import java.util.List;

public class Metamodel implements NamespaceProvider {
    private String qualifiedName;
    private List<Metamodel> dependsOn = new LinkedList<>();
    private List<MetamodelElement> elements = new LinkedList<>();

    public Metamodel(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Override
    public String namespaceQualifier() {
        return qualifiedName;
    }

    public List<Metamodel> dependsOn() {
        return this.dependsOn;
    }
    public List<MetamodelElement> getElements() {
        return this.elements;
    }

    public String getQualifiedName() {
        return this.qualifiedName;
    }
}
