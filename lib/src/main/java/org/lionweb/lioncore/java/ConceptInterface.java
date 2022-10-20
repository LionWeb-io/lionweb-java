package org.lionweb.lioncore.java;

import java.util.LinkedList;
import java.util.List;

/**
 * A ConceptInterface represents a category of entities sharing some similar characteristics.
 *
 * For example, Named would be a ConceptInterface.
 *
 * A ConceptInterface in LionWeb will be roughly equivalent to an {@link org.eclipse.emf.ecore.EClass EClass} (with the isInterface flag set to true) or
 * an MPS’s
 * <a href="https://www.jetbrains.com/help/mps/structure.html#conceptsandconceptinterfaces">Concept Interface</a> /
 * <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1169125989551">InterfaceConceptDeclaration</a> /
 * <a href="http://127.0.0.1:63320/node?ref=8865b7a8-5271-43d3-884c-6fd1d9cfdd34%2Fjava%3Aorg.jetbrains.mps.openapi.language%28MPS.OpenAPI%2F%29%2F%7ESInterfaceConcept">SConceptInterface</a>.
 */
public class ConceptInterface extends AbstractConcept {
    private List<ConceptInterface> extended = new LinkedList<>();

    public ConceptInterface(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
    }

    public List<ConceptInterface> getExtendedInterface() {
        return this.extended;
    }
}
