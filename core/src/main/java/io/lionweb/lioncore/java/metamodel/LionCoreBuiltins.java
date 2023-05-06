package io.lionweb.lioncore.java.metamodel;

public class LionCoreBuiltins extends Metamodel {
  private static LionCoreBuiltins INSTANCE = new LionCoreBuiltins();

  /** This is private to prevent instantiation and enforce the Singleton pattern. */
  private LionCoreBuiltins() {
    super("LIonCore.Builtins");
    setID("LIonCore_Builtins");
    setKey("LIonCore_Builtins");
    new PrimitiveType(this, "String");
    new PrimitiveType(this, "Boolean");
    new PrimitiveType(this, "Integer");
    new PrimitiveType(this, "JSON");
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
}
