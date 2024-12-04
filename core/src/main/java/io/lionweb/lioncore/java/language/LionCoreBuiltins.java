package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.versions.LionWebVersion;
import io.lionweb.lioncore.java.versions.LionWebVersionToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

public class LionCoreBuiltins<V extends LionWebVersionToken> extends Language<V> {
  // We may have one instance of this class per LionWeb version, and we initialize
  // them lazily
  private static final Map<LionWebVersion, LionCoreBuiltins<?>> INSTANCES = new HashMap<>();

  /** This is private to prevent instantiation and enforce the Singleton pattern. */
  private LionCoreBuiltins(@Nonnull LionWebVersion lionWebVersion) {
    super("LionCore_builtins");
    setID("LionCore-builtins");
    setKey("LionCore-builtins");
    setVersion(lionWebVersion.getVersionString());
    PrimitiveType string = new PrimitiveType(lionWebVersion, this, "String");
    new PrimitiveType(lionWebVersion, this, "Boolean");
    new PrimitiveType(lionWebVersion, this, "Integer");
    if (lionWebVersion.equals(LionWebVersion.v2023_1)) {
      new PrimitiveType(lionWebVersion, this, "JSON");
    }

    Concept<V> node = new Concept<V>( this, "Node").setID("LionCore-builtins-Node");
    node.setAbstract(true);

    Interface<V> iNamed =
        new Interface<V>( this, "INamed").setID("LionCore-builtins-INamed");
    iNamed.addFeature(
        Property.<V>createRequired(lionWebVersion, "name", string)
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

  public static <V extends LionWebVersionToken> LionCoreBuiltins<V> getInstance() {
    return getInstance(LionWebVersion.currentVersion);
  }

  public static <V extends LionWebVersionToken> LionCoreBuiltins<V> getInstance(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    if (!INSTANCES.containsKey(lionWebVersion)) {
      INSTANCES.put(lionWebVersion, new LionCoreBuiltins(lionWebVersion));
    }
    return (LionCoreBuiltins<V>) INSTANCES.get(lionWebVersion);
  }

  public static <V extends LionWebVersionToken> PrimitiveType<V> getString() {
    return (PrimitiveType<V>) getInstance().getPrimitiveTypeByName("String");
  }

  public static <V extends LionWebVersionToken> PrimitiveType<V> getString(@Nonnull LionWebVersion lionWebVersion) {
    return (PrimitiveType<V>) getInstance(lionWebVersion).getPrimitiveTypeByName("String");
  }

  public static <V extends LionWebVersionToken> PrimitiveType<V> getInteger() {
    return (PrimitiveType<V>) getInstance().getPrimitiveTypeByName("Integer");
  }

  public static <V extends LionWebVersionToken> PrimitiveType<V> getInteger(@Nonnull LionWebVersion lionWebVersion) {
    return (PrimitiveType<V>) getInstance(lionWebVersion).getPrimitiveTypeByName("Integer");
  }

  public static <V extends LionWebVersionToken> PrimitiveType<V> getBoolean() {
    return (PrimitiveType<V>) getInstance().getPrimitiveTypeByName("Boolean");
  }

  public static <V extends LionWebVersionToken> PrimitiveType<V> getBoolean(@Nonnull LionWebVersion lionWebVersion) {
    return (PrimitiveType<V>) getInstance(lionWebVersion).getPrimitiveTypeByName("Boolean");
  }

  public static <V extends LionWebVersionToken> Interface<V> getINamed() {
    return (Interface<V>) getInstance().getInterfaceByName("INamed");
  }

  public static <V extends LionWebVersionToken> Interface<V> getINamed(@Nonnull LionWebVersion lionWebVersion) {
    return (Interface<V>) getInstance(lionWebVersion).getInterfaceByName("INamed");
  }

  public static <V extends LionWebVersionToken> Concept<V> getNode() {
    return (Concept<V>) getInstance().getConceptByName("Node");
  }

  public static <V extends LionWebVersionToken> Concept<V> getNode(@Nonnull LionWebVersion lionWebVersion) {
    return (Concept<V>) getInstance(lionWebVersion).getConceptByName("Node");
  }

  public static <V extends LionWebVersionToken> PrimitiveType<V> getJSON(@Nonnull LionWebVersion lionWebVersion) {
    if (!lionWebVersion.equals(LionWebVersion.v2023_1)) {
      throw new IllegalArgumentException("JSON was present only in v2023.1");
    }
    return (PrimitiveType<V>) getInstance(lionWebVersion).getPrimitiveTypeByName("JSON");
  }
}
