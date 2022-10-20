package org.lionweb.lioncore.java;

import org.lionweb.lioncore.java.utils.Naming;

/**
 * A Feature represents a characteristic or some form of data associated with a particular concept.
 *
 * For example, an Invoice can have an associated date, a number, a connection with a customer, and it can contain
 * InvoiceLines. All of this information is represented by features.
 *
 * A Feature in LionWeb will be roughly equivalent to an {@link org.eclipse.emf.ecore.EStructuralFeature EStructuralFeature}
 * or to the combination of Properties and Links (both containment and reference links) in MPS /
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESConceptFeature">SConceptFeature</a>.
 */
public abstract class Feature implements NamespacedEntity {
    private Multiplicity multiplicity;
    private boolean derived;
    private String simpleName;
    private FeaturesContainer container;

    public Feature(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        // TODO enforce uniqueness of the name within the FeauturesContainer
        Naming.validateSimpleName(simpleName);
        this.simpleName = simpleName;
        this.container = container;
    }

    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(Multiplicity multiplicity) {
        this.multiplicity = multiplicity;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String qualifiedName() {
        return this.getContainer().namespaceQualifier() + "." + this.getSimpleName();
    }

    @Override
    public NamespaceProvider getContainer() {
        return (NamespaceProvider) container;
    }
}
