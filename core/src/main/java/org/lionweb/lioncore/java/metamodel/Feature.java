package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.Experimental;
import org.lionweb.lioncore.java.model.impl.M3Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A Feature represents a characteristic or some form of data associated with a particular concept.
 *
 * For example, an Invoice can have an associated date, a number, a connection with a customer, and it can contain
 * InvoiceLines. All of this information is represented by features.
 *
 * @see org.eclipse.emf.ecore.EStructuralFeature Ecore equivalent <i>EStructuralFeature</i>
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#conceptmembers">MPS equivalent <i>Concept members</i> in documentation</a>
 * @see org.jetbrains.mps.openapi.language.SConceptFeature MPS equivalent <i>SConceptFeature</i> in SModel
 */
public abstract class Feature<T extends M3Node> extends M3Node<T> implements NamespacedEntity {
    @Experimental
    private boolean derived;

    public Feature() {

    }

    public Feature(@Nullable String simpleName, @Nonnull String id) {
        this(simpleName, null, id);
    }

    public Feature(@Nullable String simpleName, @Nullable FeaturesContainer container, @Nonnull String id) {
        Objects.requireNonNull(id, "id should not be null");
        this.setID(id);
        // TODO verify that the container is also a NamespaceProvider
        // TODO enforce uniqueness of the name within the FeauturesContainer
        setSimpleName(simpleName);
        setContainer(container);
    }
    public Feature(@Nullable String simpleName, @Nullable FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        // TODO enforce uniqueness of the name within the FeauturesContainer
        setSimpleName(simpleName);
        setContainer(container);
    }

    public boolean isOptional() {
        return this.getPropertyValue("optional", Boolean.class, false);
    }

    public boolean isRequired() {
        return !isOptional();
    }

    public T setOptional(boolean optional) {
        setPropertyValue("optional", optional);
        return (T)this;
    }

    @Experimental
    public boolean isDerived() {
        return derived;
    }

    @Experimental
    public T setDerived(boolean derived) {
        this.derived = derived;
        return (T)this;
    }

    @Override
    public @Nullable String getSimpleName() {
        return getPropertyValue("simpleName", String.class);
    }

    public void setSimpleName(@Nullable String simpleName) {
        this.setPropertyValue("simpleName", simpleName);
    }

    @Override
    public @Nullable NamespaceProvider getContainer() {
        return getLinkSingleValue("container");
    }

    public void setContainer(@Nullable FeaturesContainer container) {
        this.setReferenceSingleValue("container", container);
    }

}
