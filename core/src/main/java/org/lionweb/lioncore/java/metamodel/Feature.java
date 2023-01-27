package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.Experimental;
import org.lionweb.lioncore.java.model.impl.M3Node;
import org.lionweb.lioncore.java.utils.Naming;

import javax.annotation.Nullable;

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

    public void setOptional(boolean optional) {
        setPropertyValue("optional", optional);
    }

    @Experimental
    public boolean isDerived() {
        return derived;
    }

    @Experimental
    public void setDerived(boolean derived) {
        this.derived = derived;
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
