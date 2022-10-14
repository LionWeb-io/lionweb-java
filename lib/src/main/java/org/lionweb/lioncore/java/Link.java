package org.lionweb.lioncore.java;

public abstract class Link extends Feature {

    public Link(String simpleName, FeaturesContainer container) {
        super(simpleName, container);
    }

    public AbstractConcept getType() {
        throw new UnsupportedOperationException();
    }
}
