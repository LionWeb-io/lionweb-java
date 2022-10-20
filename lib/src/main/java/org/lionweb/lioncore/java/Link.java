package org.lionweb.lioncore.java;

/**
 * Represent a connection to an AbstractConcept.
 *
 * An Invoice can be connected to its InvoiceLines and to a Customer.
 *
 * It is similar to Ecore's EReference. It is also similar to MPS' LinkDeclaration.
 */
public abstract class Link extends Feature {

    public Link(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
    }

    public AbstractConcept getType() {
        throw new UnsupportedOperationException();
    }

}
