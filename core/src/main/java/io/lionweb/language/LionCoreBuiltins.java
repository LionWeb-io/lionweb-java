package io.lionweb.language;

import io.lionweb.LionWebVersion;
import io.lionweb.utils.IdUtils;
import java.util.*;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class LionCoreBuiltins extends Language {
  // We may have one instance of this class per LionWeb version, and we initialize
  // them lazily
  private static final Map<LionWebVersion, LionCoreBuiltins> INSTANCES = new HashMap<>();

  /** This is private to prevent instantiation and enforce the Singleton pattern. */
  private LionCoreBuiltins(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion, "LionCore_builtins");
    final String versionIDSuffix;
    versionIDSuffix =
        lionWebVersion != LionWebVersion.v2023_1
            ? "-" + IdUtils.cleanString(lionWebVersion.getVersionString())
            : "";

    setID("LionCore-builtins" + versionIDSuffix);
    setKey("LionCore-builtins");
    setVersion(lionWebVersion.getVersionString());
    List<PrimitiveType> primitiveTypes = new ArrayList<>();
    PrimitiveType string = new PrimitiveType(lionWebVersion).setName("String");
    primitiveTypes.add(string);
    primitiveTypes.add(new PrimitiveType(lionWebVersion).setName("Boolean"));
    primitiveTypes.add(new PrimitiveType(lionWebVersion).setName("Integer"));
    if (lionWebVersion.equals(LionWebVersion.v2023_1)) {
      primitiveTypes.add(new PrimitiveType(lionWebVersion).setName("JSON"));
    }
    Consumer<LanguageEntity<?>> idAndKeySetter =
        (e) -> {
          if (e.getID() == null) {
            e.setID("LionCore-builtins-" + e.getName() + versionIDSuffix);
          }
          if (e.getKey() == null) {
            e.setKey("LionCore-builtins-" + e.getName());
          }
        };
    primitiveTypes.forEach(
        e -> {
          idAndKeySetter.accept(e);
          this.addElement(e);
        });

    Concept node = new Concept(this, "Node", "LionCore-builtins-Node" + versionIDSuffix);
    node.setAbstract(true);

    Interface iNamed = new Interface(this, "INamed", "LionCore-builtins-INamed" + versionIDSuffix);
    iNamed.addFeature(
        Property.createRequired(lionWebVersion, "name", string)
            .setID("LionCore-builtins-INamed-name" + versionIDSuffix)
            .setKey("LionCore-builtins-INamed-name"));

    this.getElements().forEach(idAndKeySetter::accept);
  }

  public static LionCoreBuiltins getInstance() {
    return getInstance(LionWebVersion.currentVersion);
  }

  public static LionCoreBuiltins getInstance(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    if (!INSTANCES.containsKey(lionWebVersion)) {
      INSTANCES.put(lionWebVersion, new LionCoreBuiltins(lionWebVersion));
    }
    return INSTANCES.get(lionWebVersion);
  }

  public static PrimitiveType getString() {
    return getInstance().getPrimitiveTypeByName("String");
  }

  public static PrimitiveType getString(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).getPrimitiveTypeByName("String");
  }

  public static PrimitiveType getInteger() {
    return getInstance().getPrimitiveTypeByName("Integer");
  }

  public static PrimitiveType getInteger(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).getPrimitiveTypeByName("Integer");
  }

  public static PrimitiveType getBoolean() {
    return getInstance().getPrimitiveTypeByName("Boolean");
  }

  public static PrimitiveType getBoolean(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).getPrimitiveTypeByName("Boolean");
  }

  public static Interface getINamed() {
    return getInstance().getInterfaceByName("INamed");
  }

  public static Interface getINamed(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).getInterfaceByName("INamed");
  }

  public static Concept getNode() {
    return getInstance().getConceptByName("Node");
  }

  public static Concept getNode(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).getConceptByName("Node");
  }

  public static PrimitiveType getJSON(@Nonnull LionWebVersion lionWebVersion) {
    if (!lionWebVersion.equals(LionWebVersion.v2023_1)) {
      throw new IllegalArgumentException("JSON was present only in v2023.1");
    }
    return getInstance(lionWebVersion).getPrimitiveTypeByName("JSON");
  }
}
