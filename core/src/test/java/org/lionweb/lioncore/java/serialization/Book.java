package org.lionweb.lioncore.java.serialization;

import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.model.ReferenceValue;
import org.lionweb.lioncore.java.model.impl.DynamicNode;

public class Book extends DynamicNode {

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
        this.setPropertyValue(getConcept().getPropertyByName("pages"), pages);
        return this;
    }

    public String getTitle() {
        return (String)this.getPropertyValueByName("title");
    }

    public void setAuthor(Writer author) {
        this.addReferenceValue(getConcept().getReferenceByName("author"), new ReferenceValue(author, author.getName()));
    }

    @Override
    public Concept getConcept() {
        return LibraryMetamodel.BOOK;
    }
}
