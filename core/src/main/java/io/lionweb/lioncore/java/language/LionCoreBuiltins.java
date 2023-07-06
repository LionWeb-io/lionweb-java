package io.lionweb.lioncore.java.language;

public class LionCoreBuiltins extends Language {
  private static LionCoreBuiltins INSTANCE = new LionCoreBuiltins();

  /** This is private to prevent instantiation and enforce the Singleton pattern. */
  private LionCoreBuiltins() {
    super("LIonCore.Builtins");
    setID("LIonCore_Builtins");
    setKey("LIonCore-builtins");
    setVersion("1");
    PrimitiveType string = new PrimitiveType(this, "String");
    new PrimitiveType(this, "Boolean");
    new PrimitiveType(this, "Integer");
    new PrimitiveType(this, "JSON");
    ConceptInterface iNamed = new ConceptInterface(this, "INamed");
    iNamed.addFeature(Property.createRequired("name", string).setKey("LIonCore-builtins-INamed-name"));
    this.getElements()
        .forEach(
            e -> {
              e.setID("LIonCore_M3_" + e.getName());
              e.setKey(e.getName());
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
}
