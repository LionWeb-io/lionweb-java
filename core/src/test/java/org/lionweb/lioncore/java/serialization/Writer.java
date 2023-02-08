package org.lionweb.lioncore.java.serialization;

import org.lionweb.lioncore.java.metamodel.Concept;

public class Writer extends DummyNode {

    public Writer(String id) {
        super(id, LibraryMetamodel.WRITER);
    }

    public Writer(String id, String name) {
        super(id, LibraryMetamodel.WRITER);
        setName(name);
    }

    public void setName(String name) {
        this.setPropertyValue(getConcept().getPropertyByName("name"), name);
    }

    @Override
    public Concept getConcept() {
        return LibraryMetamodel.WRITER;
    }
}
