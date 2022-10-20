package org.lionweb.lioncore.java;

/**
 * Represents a relation between a containing {@link FeaturesContainer} and a contained {@link AbstractConcept}.
 *
 * Between an IfStatement and its condition there is a Containment relation.
 *
 * Differently from an EReference there is no container flag and resolveProxies flag.
 *
 * @see org.eclipse.emf.ecore.EReference Ecore equivalent <i>EReference</i> (with the <tt>containment</tt> flag set to <tt>true</tt>)
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#children">MPS equivalent <i>Child</i> in documentation</a>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">MPS equivalent <i>LinkDeclaration</i> in local MPS (with <tt>metaClass</tt> having value <tt>aggregation</tt>)</a>
 * @see org.jetbrains.mps.openapi.language.SContainmentLink MPS equivalent <i>SContainmentLink</i> in SModel
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
