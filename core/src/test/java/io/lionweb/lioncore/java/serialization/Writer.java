package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
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
    this.setPropertyValue(getClassifier().getPropertyByName("name"), name);
  }

  public String getName() {
    return (String) ClassifierInstanceUtils.getPropertyValueByName(this, "name");
  }

  @Override
  public Concept getClassifier() {
    return LibraryLanguage.WRITER;
  }
}
