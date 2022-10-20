package org.lionweb.lioncore.java;

/**
 * This represents a relation between an {@link FeaturesContainer} and referred {@link AbstractConcept}.
 *
 * A VariableReference may have a Reference to a VariableDeclaration.
 *
 * A Containment is similar to an Ecore’s {@link org.eclipse.emf.ecore.EReference EReference} with the containment flag set to false.
 * In MPS, it's a
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">LinkDeclaration</a>
 * with metaClass having value reference /
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESReferenceLink">SReferenceLink</a>.
 */
public class Reference extends Link {
    private Reference specialized;

    public Reference(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
    }

    public Reference getSpecialized() {
        return specialized;
    }

    public void setSpecialized(Reference specialized) {
        // TODO check which limitations there are: should have the same name? Should it belong
        //      to an ancestor of the FeaturesContainer holding this Containment?
        this.specialized = specialized;
    }

    @Override
    public void setMultiplicity(Multiplicity multiplicity) {
        // TODO check constraint on multiplicity
        super.setMultiplicity(multiplicity);
    }
}
