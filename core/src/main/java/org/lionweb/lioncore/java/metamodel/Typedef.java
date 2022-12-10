package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.Experimental;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.self.LionCore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @see <a href="https://www.jetbrains.com/help/mps/structure.html#constraineddatatypes">MPS equivalent <i>Constrained Data Type</i> in documentation</a>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1082978499127">MPS equivalent <i>ConstrainedDataTypeDeclaration</i> in local MPS</a>
 */
@Experimental
public class Typedef extends DataType {
    private PrimitiveType primitiveType;

    public Typedef() {
        super();
        setConcept(LionCore.getTypedef());
    }

    public Typedef(Metamodel metamodel, String simpleName) {
        super(metamodel, simpleName);
        setConcept(LionCore.getTypedef());
    }

    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(PrimitiveType primitiveType) {
        this.primitiveType = primitiveType;
    }

    @Override
    public List<Node> getReferredNodes(Reference reference) {
        if (reference == LionCore.getTypedef().getReferenceByName("primitiveType")) {
            return Arrays.asList(getPrimitiveType()).stream().filter(e -> e != null).collect(Collectors.toList());
        }
        return super.getReferredNodes(reference);
    }

    @Override
    public void addReferredNode(Reference reference, Node referredNode) {
        if (reference == LionCore.getTypedef().getReferenceByName("primitiveType")) {
            this.setPrimitiveType((PrimitiveType) referredNode);
            return;
        }
        super.addReferredNode(reference, referredNode);
    }
}
