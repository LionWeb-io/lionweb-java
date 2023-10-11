package io.lionweb.lioncore.java.language;

public class LionCoreBuiltins extends Language {
  private static LionCoreBuiltins INSTANCE = new LionCoreBuiltins();

  /** This is private to prevent instantiation and enforce the Singleton pattern. */
  private LionCoreBuiltins() {
    super("LionCore.builtins");
    setID("LionCore-builtins");
    setKey("LionCore-builtins");
    setVersion("2023.1");
    PrimitiveType string = new PrimitiveType(this, "String");
    new PrimitiveType(this, "Boolean");
    new PrimitiveType(this, "Integer");
    new PrimitiveType(this, "JSON");

    Concept node = new Concept(this, "Node").setID("LionCore-builtins-Node");
    node.setAbstract(true);

    ConceptInterface iNamed =
        new ConceptInterface(this, "INamed").setID("LionCore-builtins-INamed");
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

  public static ConceptInterface getINamed() {
    return INSTANCE.getConceptInterfaceByName("INamed");
  }

  public static Concept getNode() {
    return INSTANCE.getConceptByName("Node");
  }
}
