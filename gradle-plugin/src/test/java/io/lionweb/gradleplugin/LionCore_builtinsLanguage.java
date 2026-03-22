package io.lionweb.gradleplugin;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Concept;
import io.lionweb.language.Interface;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.language.PrimitiveType;
import io.lionweb.language.Property;

public class LionCore_builtinsLanguage extends Language {
  private static LionCore_builtinsLanguage INSTANCE;

  private LionCore_builtinsLanguage() {
    super(LionWebVersion.v2023_1);
    this.setName("LionCore_builtins");
    this.setVersion("2023.1");
    this.setID("LionCore-builtins");
    this.setKey("LionCore-builtins");
    createElements();
    initNode();
    initINamed();
  }

  public static LionCore_builtinsLanguage getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new LionCore_builtinsLanguage();
    }
    return INSTANCE;
  }

  public Concept getNode() {
    return this.requireConceptByName("Node");
  }

  private void initNode() {
    Concept concept = this.requireConceptByName("Node");
    concept.setAbstract(true);
    concept.setPartition(false);
  }

  public Interface getINamed() {
    return this.requireInterfaceByName("INamed");
  }

  private void initINamed() {
    Interface interf = this.requireInterfaceByName("INamed");
    Property name = new Property("name", interf, "LionCore-builtins-INamed-name");
    name.setKey("LionCore-builtins-INamed-name");
    name.setType(LionCoreBuiltins.getString(LionWebVersion.v2023_1));
    name.setOptional(false);
  }

  private void createElements() {
    new Concept(this, "Node", "LionCore-builtins-Node", "LionCore-builtins-Node");
    ;
    new Interface(this, "INamed", "LionCore-builtins-INamed", "LionCore-builtins-INamed");
    ;
    PrimitiveType String = new PrimitiveType(this, "String", "LionCore-builtins-String");
    ;
    String.setKey("LionCore-builtins-String");
    PrimitiveType Boolean = new PrimitiveType(this, "Boolean", "LionCore-builtins-Boolean");
    ;
    Boolean.setKey("LionCore-builtins-Boolean");
    PrimitiveType Integer = new PrimitiveType(this, "Integer", "LionCore-builtins-Integer");
    ;
    Integer.setKey("LionCore-builtins-Integer");
    PrimitiveType JSON = new PrimitiveType(this, "JSON", "LionCore-builtins-JSON");
    ;
    JSON.setKey("LionCore-builtins-JSON");
  }
}
