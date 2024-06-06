package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.model.impl.DynamicNode;

public class Library extends DynamicNode {

  public Library(String id, String name) {
    super(id, LibraryLanguage.LIBRARY);
    setName(name);
  }

  @Override
  public Concept getClassifier() {
    return LibraryLanguage.LIBRARY;
  }

  public void addBook(Book book) {
    this.addChild(getClassifier().getContainmentByName("books"), book);
  }

  public void setName(String name) {
    this.setPropertyValue(getClassifier().getPropertyByName("name"), name);
  }
}
