package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.impl.M3Node;

import javax.annotation.Nullable;

/**
 * Represent a connection to an {@link FeaturesContainer}.
 *
 * An Invoice can be connected to its InvoiceLines and to a Customer.
 *
 * @see org.eclipse.emf.ecore.EReference Ecore equivalent <i>EReference</i>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">MPS equivalent <i>LinkDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SAbstractLink MPS equivalent <i>SAbstractLink</i> in SModel
 */
public abstract class Link<T extends M3Node> extends Feature<T> {
    public Link() {
        super();
    }

    public Link(@Nullable String simpleName, @Nullable FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
    }

    public boolean isMultiple() {
        return getPropertyValue("multiple", Boolean.class, false);
    }

    public void setMultiple(boolean multiple) {
        this.setPropertyValue("multiple", multiple);
    }

    public @Nullable FeaturesContainer getType() {
        return getLinkSingleValue("type");
    }

    public void setType(@Nullable FeaturesContainer type) {
        this.setReferenceSingleValue("type", type);
    }
}
