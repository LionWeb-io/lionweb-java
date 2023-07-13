package io.lionweb.lioncore.java.language;

public class LionCoreBuiltins extends Language {
  private static LionCoreBuiltins INSTANCE = new LionCoreBuiltins();

  /** This is private to prevent instantiation and enforce the Singleton pattern. */
  private LionCoreBuiltins() {
    super("LIonCore.builtins");
    setID("LIonCore-builtins");
    setKey("LIonCore-builtins");
    setVersion("1");
    PrimitiveType string = new PrimitiveType(this, "String");
    new PrimitiveType(this, "Boolean");
    new PrimitiveType(this, "Integer");
    new PrimitiveType(this, "JSON");

    Concept node = new Concept(this, "Node").setID("LIonCore-builtins-Node");
    node.setAbstract(true);

    ConceptInterface iNamed =
        new ConceptInterface(this, "INamed").setID("LIonCore-builtins-INamed");
    iNamed.addFeature(
        Property.createRequired("name", string)
            .setID("LIonCore-builtins-INamed-name")
            .setKey("LIonCore-builtins-INamed-name"));

    this.getElements()
        .forEach(
            e -> {
              if (e.getID() == null) {
                e.setID("LIonCore-builtins-" + e.getName());
              }
              if (e.getKey() == null) {
                e.setKey("LIonCore-builtins-" + e.getName());
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
