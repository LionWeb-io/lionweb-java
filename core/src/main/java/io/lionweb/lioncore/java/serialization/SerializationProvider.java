package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.self.LionCore;
import javax.annotation.Nonnull;

public class SerializationProvider {

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getStandardJsonSerialization() {
    return getStandardJsonSerialization(LionWebVersion.currentVersion);
  }

  public static JsonSerialization getStandardJsonSerialization(
      @Nonnull LionWebVersion lionWebVersion) {
    JsonSerialization serialization = new JsonSerialization(lionWebVersion);
    standardInitialization(serialization);
    return serialization;
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getBasicJsonSerialization() {
    return new JsonSerialization();
  }

  public static JsonSerialization getBasicJsonSerialization(
      @Nonnull LionWebVersion lionWebVersion) {
    return new JsonSerialization(lionWebVersion);
  }

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static ProtoBufSerialization getStandardProtoBufSerialization() {
    ProtoBufSerialization serialization = new ProtoBufSerialization();
    standardInitialization(serialization);
    return serialization;
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static ProtoBufSerialization getBasicProtoBufSerialization() {
    return new ProtoBufSerialization();
  }

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static FlatBuffersSerialization getStandardFlatBuffersSerialization() {
    FlatBuffersSerialization serialization = new FlatBuffersSerialization();
    standardInitialization(serialization);
    return serialization;
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static FlatBuffersSerialization getBasicFlatBuffersSerialization() {
    return new FlatBuffersSerialization();
  }

  protected static void standardInitialization(AbstractSerialization serialization) {
    serialization.classifierResolver.registerLanguage(
        LionCore.getInstance(serialization.getLionWebVersion()));
    serialization.instantiator.registerLionCoreCustomDeserializers(
        serialization.getLionWebVersion());
    serialization.primitiveValuesSerialization
        .registerLionBuiltinsPrimitiveSerializersAndDeserializers(
            serialization.getLionWebVersion());
    serialization.instanceResolver.addAll(
        LionCore.getInstance(serialization.getLionWebVersion()).thisAndAllDescendants());
    serialization.instanceResolver.addAll(
        LionCoreBuiltins.getInstance(serialization.getLionWebVersion()).thisAndAllDescendants());
  }
}
