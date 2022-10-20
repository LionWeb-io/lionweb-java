package org.lionweb.lioncore.java;

import org.lionweb.lioncore.java.utils.Naming;

/**
 * A MetamodelElement is an element with an identity within a {@link Metamodel}.
 *
 * For example, Invoice, Currency, Named, or String could be MetamodelElements.
 *
 * MetamodelElement is similar to Ecore's {@link org.eclipse.emf.ecore.EClassifier EClassifier}.
 * MetamodelElement is similar to MPS'
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1588368162880706270">IStructureElement</a> /
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESElement">SElement</a>.
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
