package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.utils.Naming;
import org.lionweb.lioncore.java.utils.Validatable;

/**
 * A MetamodelElement is an element with an identity within a {@link Metamodel}.
 *
 * For example, Invoice, Currency, Named, or String could be MetamodelElements.
 *
 * @see org.eclipse.emf.ecore.EClassifier Ecore equivalent <i>EClassifier</i>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1588368162880706270">MPS equivalent <i>IStructureElement</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SElement MPS equivalent <i>SElement</i> in SModel
 */
public abstract class MetamodelElement implements NamespacedEntity, Validatable {
    private Metamodel metamodel;
    private String simpleName;

    public MetamodelElement() {

    }

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

    @Override
    public Validatable.ValidationResult validate() {
        return new Validatable.ValidationResult()
                .checkForError(() -> getSimpleName() == null, "Simple name not set")
                .checkForError(() -> getMetamodel() == null, "Metamodel not set");
    }
}
