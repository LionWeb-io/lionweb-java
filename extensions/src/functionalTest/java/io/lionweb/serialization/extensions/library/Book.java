package io.lionweb.serialization.extensions.library;

import io.lionweb.language.Concept;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.GenericReferenceValue;
import io.lionweb.model.impl.DynamicNode;

public class Book extends DynamicNode {

  public Book(String id) {
    super(id, LibraryLanguage.BOOK);
  }

  public Book(String id, String title, Writer author) {
    super(id, LibraryLanguage.BOOK);
    setTitle(title);
    setAuthor(author);
  }

  public void setTitle(String title) {
    this.setPropertyValue(getClassifier().getPropertyByName("title"), title);
  }

  public Book setPages(int pages) {
    this.setPropertyValue(getClassifier().getPropertyByName("pages"), pages);
    return this;
  }

  public String getTitle() {
    return (String) ClassifierInstanceUtils.getPropertyValueByName(this, "title");
  }

  public void setAuthor(Writer author) {
    this.addReferenceValue(
        getClassifier().getReferenceByName("author"),
        new GenericReferenceValue(author, author.getName()));
  }

  @Override
  public Concept getClassifier() {
    return LibraryLanguage.BOOK;
  }
}
