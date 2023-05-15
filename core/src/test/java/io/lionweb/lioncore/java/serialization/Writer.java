package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.model.impl.DynamicNode;

public class Writer extends DynamicNode {

  public Writer(String id) {
    super(id, LibraryLanguage.WRITER);
  }

  public Writer(String id, String name) {
    super(id, LibraryLanguage.WRITER);
    setName(name);
  }

  protected Writer(String id, String name, Concept concept) {
    super(id, concept);
    setName(name);
  }

  public void setName(String name) {
    this.setPropertyValue(getConcept().getPropertyByName("name"), name);
  }

  public String getName() {
    return (String) this.getPropertyValueByName("name");
  }

  @Override
  public Concept getConcept() {
    return LibraryLanguage.WRITER;
  }
}
