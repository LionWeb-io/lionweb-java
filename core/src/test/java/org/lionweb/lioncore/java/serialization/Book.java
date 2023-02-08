package org.lionweb.lioncore.java.serialization;

import org.lionweb.lioncore.java.metamodel.Concept;

public class Book extends DummyNode {

    public Book(String id) {
        super(id, LibraryMetamodel.BOOK);
    }

    public Book(String id, String title, Writer author) {
        super(id, LibraryMetamodel.BOOK);
        setTitle(title);
        setAuthor(author);
    }

    public void setTitle(String title) {
        this.setPropertyValue(getConcept().getPropertyByName("title"), title);
    }

    public Book setPages(int pages) {
        this.setPropertyValue(getConcept().getPropertyByName("pages"), Integer.toString(pages));
        return this;
    }

    public String getTitle() {
        return (String)this.getPropertyValueByName("title");
    }

    public void setAuthor(Writer author) {
        this.addReferredNode(getConcept().getReferenceByName("author"), author);
    }

    @Override
    public Concept getConcept() {
        return LibraryMetamodel.BOOK;
    }
}
