package org.lionweb.lioncore.java;

import java.util.LinkedList;
import java.util.List;

/**
 * A Concept represents a category of entities sharing the same structure.
 *
 * For example, Invoice would be a Concept. Single entities could be Concept instances, such as Invoice #1/2022.
 *
 * A Concept in LionWeb will be roughly equivalent to an {@link org.eclipse.emf.ecore.EClass EClass} (with the isInterface flag set to false) or
 * MPS’s
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489090640">ConceptDeclaration</a> /
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESConcept">SConcept</a>.
 */
public class Concept extends AbstractConcept {
    private boolean isAbstract;
    private Concept extended;
    private List<ConceptInterface> implemented = new LinkedList<>();

    public Concept(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public boolean isAbstract() {
        return this.isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    // TODO should this return BaseConcept when extended is equal null?
    public Concept getExtendedConcept() {
        return this.extended;
    }

    public List<ConceptInterface> getImplemented() {
        return this.implemented;
    }

    // TODO should we verify the Concept does not extend itself, even indirectly?
    public void setExtendedConcept(Concept extended) {
        this.extended = extended;
    }


}
