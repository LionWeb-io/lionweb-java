package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.metamodel.Concept;
import io.lionweb.lioncore.java.model.impl.DynamicNode;

public class Writer extends DynamicNode {

  public Writer(String id) {
    super(id, LibraryMetamodel.WRITER);
  }

  public Writer(String id, String name) {
    super(id, LibraryMetamodel.WRITER);
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
    return LibraryMetamodel.WRITER;
  }
}
