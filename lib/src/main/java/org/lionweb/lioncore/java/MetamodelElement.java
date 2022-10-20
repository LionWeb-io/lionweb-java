package org.lionweb.lioncore.java;

import org.lionweb.lioncore.java.utils.Naming;

/**
 * A MetamodelElement is an element with an identity within a Metamodel.
 *
 * For example, Invoice, Currency, Named, or String could be MetamodelElements.
 *
 * MetamodelElement is similar to Ecore's EClassifier.
 * MetamodelElement is similar to MPS' IStructureElement.
 */
public abstract class MetamodelElement implements NamespacedEntity {
    private Metamodel metamodel;
    private String simpleName;

    public MetamodelElement(Metamodel metamodel, String simpleName) {
        // TODO enforce uniqueness of the name within the Metamodel
        Naming.validateSimpleName(simpleName);
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
