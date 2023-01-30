package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.impl.M3Node;
import org.lionweb.lioncore.java.utils.Naming;

import javax.annotation.Nullable;

/**
 * A MetamodelElement is an element with an identity within a {@link Metamodel}.
 *
 * For example, Invoice, Currency, Named, or String could be MetamodelElements.
 *
 * @see org.eclipse.emf.ecore.EClassifier Ecore equivalent <i>EClassifier</i>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1588368162880706270">MPS equivalent <i>IStructureElement</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SElement MPS equivalent <i>SElement</i> in SModel
 */
public abstract class MetamodelElement<T extends M3Node> extends M3Node<T> implements NamespacedEntity {

    public MetamodelElement() {

    }

    public MetamodelElement(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        // TODO enforce uniqueness of the name within the Metamodel
        Naming.validateSimpleName(simpleName);
        this.setMetamodel(metamodel);
        this.setSimpleName(simpleName);
    }

    // TODO consider making this a derived feature just casting the parent
    public @Nullable Metamodel getMetamodel() {
        return this.getLinkSingleValue("metamodel");
    }

    public void setMetamodel(@Nullable Metamodel metamodel) {
        this.setReferenceSingleValue("metamodel", metamodel);
    }

    @Override
    public @Nullable String getSimpleName() {
        return this.getPropertyValue("simpleName", String.class);
    }

    public void setSimpleName(String simpleName) {
        this.setPropertyValue("simpleName", simpleName);
    }

    @Override
    public @Nullable NamespaceProvider getContainer() {
        return this.getMetamodel();
    }

}
