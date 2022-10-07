package lioncore.java;

import java.util.List;

public abstract class MetamodelElement implements NamespacedEntity {
    protected Metamodel metamodel;
    protected String simpleName;

    public MetamodelElement(Metamodel metamodel, String simpleName) {
        this.metamodel = metamodel;
        this.simpleName = simpleName;
    }

    public Metamodel getMetamodel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSimpleName() {
        return this.simpleName;
    }

    @Override
    public String qualifiedName() {
        return this.getContainer().namespaceQualifier() + "." + this.getSimpleName();
    }

    @Override
    public NamespaceProvider getContainer() {
        return this.metamodel;
    }
}
