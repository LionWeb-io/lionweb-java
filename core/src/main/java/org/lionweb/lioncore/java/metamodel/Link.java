package org.lionweb.lioncore.java.metamodel;

import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.self.LionCore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represent a connection to an {@link FeaturesContainer}.
 *
 * An Invoice can be connected to its InvoiceLines and to a Customer.
 *
 * @see org.eclipse.emf.ecore.EReference Ecore equivalent <i>EReference</i>
 * @see <a href="http://127.0.0.1:63320/node?ref=r%3A00000000-0000-4000-0000-011c89590292%28jetbrains.mps.lang.structure.structure%29%2F1071489288298">MPS equivalent <i>LinkDeclaration</i> in local MPS</a>
 * @see org.jetbrains.mps.openapi.language.SAbstractLink MPS equivalent <i>SAbstractLink</i> in SModel
 */
public abstract class Link extends Feature {
    private boolean multiple;
    private FeaturesContainer type;

    public Link() {
        super();
    }

    public Link(String simpleName, FeaturesContainer container) {
        // TODO verify that the container is also a NamespaceProvider
        super(simpleName, container);
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public FeaturesContainer getType() {
        return this.type;
    }

    public void setType(FeaturesContainer type) {
        this.type = type;
    }

    @Override
    public Object getPropertyValue(Property property) {
        if (property == LionCore.getLink().getPropertyByName("multiple")) {
            return this.isMultiple();
        }
        return super.getPropertyValue(property);
    }

    @Override
    public void setPropertyValue(Property property, Object value) {
        if (property == LionCore.getLink().getPropertyByName("multiple")) {
            setMultiple((Boolean) value);
            return;
        }
        super.setPropertyValue(property, value);
    }

    @Override
    public List<Node> getReferredNodes(Reference reference) {
        if (reference == LionCore.getAnnotation().getReferenceByName("type")) {
            return Arrays.asList(this.getType()).stream().filter(e -> e != null).collect(Collectors.toList());
        }
        return super.getReferredNodes(reference);
    }

    @Override
    public void addReferredNode(Reference reference, Node referredNode) {
        if (reference == LionCore.getAnnotation().getReferenceByName("type")) {
            this.setType((FeaturesContainer) referredNode);
            return;
        }
        super.addReferredNode(reference, referredNode);
    }

}
