package org.lionweb.lioncore.java;

/**
 * Represent a connection to an {@link AbstractConcept}.
 *
 * An Invoice can be connected to its InvoiceLines and to a Customer.
 *
 * It is similar to Ecore's {@link org.eclipse.emf.ecore.EReference EReference}.
 * It is also similar to MPS'
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">LinkDeclaration</a> /
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESAbstractLink">SAbstractLink</a>.
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
