package org.lionweb.lioncore.java.serialization;

import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.model.impl.DynamicNode;

public class Library extends DynamicNode {

  public Library(String id, String name) {
    super(id, LibraryMetamodel.LIBRARY);
    setName(name);
  }

  @Override
  public Concept getConcept() {
    return LibraryMetamodel.LIBRARY;
  }

  public void addBook(Book book) {
    this.addChild(getConcept().getContainmentByName("books"), book);
  }

  public void setName(String name) {
    this.setPropertyValue(getConcept().getPropertyByName("name"), name);
  }
}
