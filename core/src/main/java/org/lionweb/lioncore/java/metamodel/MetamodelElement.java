package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.ReferenceValue;
import org.lionweb.lioncore.java.model.impl.M3Node;
import org.lionweb.lioncore.java.utils.Naming;

import javax.annotation.Nonnull;
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
public abstract class MetamodelElement<T extends M3Node> extends M3Node<T> implements NamespacedEntity, HasKey<T> {

    public MetamodelElement() {

    }

    public MetamodelElement(@Nullable Metamodel metamodel, @Nullable String simpleName, @Nonnull String id) {
        this(metamodel, simpleName);
        this.setID(id);
    }

    public MetamodelElement(@Nullable Metamodel metamodel, @Nullable String simpleName) {
        // TODO enforce uniqueness of the name within the Metamodel
        this.setMetamodel(metamodel);
        this.setSimpleName(simpleName);
    }

    // TODO consider making this a derived feature just casting the parent
    public @Nullable Metamodel getMetamodel() {
        return (Metamodel) getParent();
    }

    // TODO remove me
    public T setMetamodel(@Nullable Metamodel metamodel) {
        if (metamodel == null) {
            this.setReferenceSingleValue("metamodel", null);
        } else {
            this.setReferenceSingleValue("metamodel", new ReferenceValue(metamodel, metamodel.getName()));
        }
        return (T)this;
    }

    @Override
    public @Nullable String getSimpleName() {
        return this.getPropertyValue("simpleName", String.class);
    }

    public T setSimpleName(String simpleName) {
        this.setPropertyValue("simpleName", simpleName);
        return (T)this;
    }

    @Override
    public @Nullable NamespaceProvider getContainer() {
        return (NamespaceProvider) this.getParent();
    }

    @Override
    public String getKey() {
        return this.getPropertyValue("key", String.class);
    }

    @Override
    public T setKey(String key) {
        setPropertyValue("key", key);
        return (T) this;
    }

    protected Object getDerivedValue(Property property) {
        if (property.getKey().equals(this.getConcept().getPropertyByName("qualifiedName").getKey())) {
            return qualifiedName();
        }
        return null;
    }
}
