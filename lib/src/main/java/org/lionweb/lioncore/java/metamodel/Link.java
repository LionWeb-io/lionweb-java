package org.lionweb.lioncore.java.metamodel;

/**
 * Represent a connection to an {@link AbstractConcept}.
 *
 * An Invoice can be connected to its InvoiceLines and to a Customer.
 *
 * @see org.eclipse.emf.ecore.EReference Ecore equivalent <i>EReference</i>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">MPS equivalent <i>LinkDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SAbstractLink MPS equivalent <i>SAbstractLink</i> in SModel
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
