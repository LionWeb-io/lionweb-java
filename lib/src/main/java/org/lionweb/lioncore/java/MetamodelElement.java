package org.lionweb.lioncore.java;

public abstract class MetamodelElement implements NamespacedEntity {
    private Metamodel metamodel;
    private String simpleName;

    public MetamodelElement(Metamodel metamodel, String simpleName) {
        this.metamodel = metamodel;
        this.simpleName = simpleName;
    }

    public Metamodel getMetamodel() {
        return this.metamodel;
    }

    @Override
    public String getSimpleName() {
        return this.simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
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
