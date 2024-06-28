package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.serialization.JsonSerialization;

public class LionCoreBuiltins extends Language {
  private static final LionCoreBuiltins INSTANCE = new LionCoreBuiltins();

  /** This is private to prevent instantiation and enforce the Singleton pattern. */
  private LionCoreBuiltins() {
    super("LionCore_builtins");
    setID("LionCore-builtins");
    setKey("LionCore-builtins");
    // TODO we should move to the current version
    setVersion(JsonSerialization.SERIALIZATION_FORMAT_2023_1);
    PrimitiveType string = new PrimitiveType(this, "String");
    new PrimitiveType(this, "Boolean");
    new PrimitiveType(this, "Integer");
    new PrimitiveType(this, "JSON");

    Concept node = new Concept(this, "Node").setID("LionCore-builtins-Node");
    node.setAbstract(true);

    Interface iNamed = new Interface(this, "INamed").setID("LionCore-builtins-INamed");
    iNamed.addFeature(
        Property.createRequired("name", string)
            .setID("LionCore-builtins-INamed-name")
            .setKey("LionCore-builtins-INamed-name"));

    this.getElements()
        .forEach(
            e -> {
              if (e.getID() == null) {
                e.setID("LionCore-builtins-" + e.getName());
              }
              if (e.getKey() == null) {
                e.setKey("LionCore-builtins-" + e.getName());
              }
            });
  }

  public static LionCoreBuiltins getInstance() {
    return INSTANCE;
  }

  public static PrimitiveType getString() {
    return INSTANCE.getPrimitiveTypeByName("String");
  }

  public static PrimitiveType getInteger() {
    return INSTANCE.getPrimitiveTypeByName("Integer");
  }

  public static PrimitiveType getBoolean() {
    return INSTANCE.getPrimitiveTypeByName("Boolean");
  }

  public static PrimitiveType getJSON() {
    return INSTANCE.getPrimitiveTypeByName("JSON");
  }

  public static Interface getINamed() {
    return INSTANCE.getInterfaceByName("INamed");
  }

  public static Concept getNode() {
    return INSTANCE.getConceptByName("Node");
  }
}
