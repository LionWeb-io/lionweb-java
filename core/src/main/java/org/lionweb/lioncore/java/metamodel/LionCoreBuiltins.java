package org.lionweb.lioncore.java.metamodel;

public class LionCoreBuiltins extends Metamodel {
    private static LionCoreBuiltins INSTANCE = new LionCoreBuiltins();

    /**
     * This is private to prevent instantiation and enforce the Singleton pattern.
     */
    private LionCoreBuiltins() {
        super("org.lionweb.Builtins");
        setID("lioncore_builtins");
        this.getElements().add(new PrimitiveType(this, "String"));
        this.getElements().add(new PrimitiveType(this, "Boolean"));
        this.getElements().add(new PrimitiveType(this, "Integer"));
        this.getElements().add(new PrimitiveType(this, "JSON"));
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
