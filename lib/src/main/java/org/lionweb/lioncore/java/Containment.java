package org.lionweb.lioncore.java;

/**
 * Represents a relation between a containing {@link FeaturesContainer} and a contained {@link AbstractConcept}.
 *
 * Between an IfStatement and its condition there is a Containment relation.
 *
 * A Containment is similar to an ECore’s {@link org.eclipse.emf.ecore.EReference EReference} with the containment flag set to true.
 * Differently from an EReference there is no container flag and resolveProxies flag.
 * A Containment is similar to an MPS’s
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">LinkDeclaration</a>
 * with metaClass having value aggregation /
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESContainmentLink">SContainmentLink</a>.
 */
public class Containment extends Link {
    private Containment specialized;

    public Containment(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
    }

    public Containment getSpecialized() {
        return specialized;
    }

    public void setSpecialized(Containment specialized) {
        // TODO check which limitations there are: should have the same name? Should it belong
        //      to an ancestor of the FeaturesContainer holding this Containment?
        this.specialized = specialized;
    }

}
