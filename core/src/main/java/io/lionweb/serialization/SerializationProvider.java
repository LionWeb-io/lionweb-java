package io.lionweb.serialization;

import io.lionweb.LionWebVersion;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.lioncore.LionCore;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SerializationProvider {

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getStandardJsonSerialization() {
    return getStandardJsonSerialization(LionWebVersion.currentVersion);
  }

  public static JsonSerialization getStandardJsonSerialization(
      @Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
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
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    return new JsonSerialization(lionWebVersion);
  }

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static ProtoBufSerialization getStandardProtoBufSerialization(
      @Nonnull LionWebVersion lionWebVersion) {
    ProtoBufSerialization serialization = new ProtoBufSerialization(lionWebVersion);
    standardInitialization(serialization);
    return serialization;
  }

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static ProtoBufSerialization getStandardProtoBufSerialization() {
    return getStandardProtoBufSerialization(LionWebVersion.currentVersion);
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static ProtoBufSerialization getBasicProtoBufSerialization(
      @Nonnull LionWebVersion lionWebVersion) {
    return new ProtoBufSerialization(lionWebVersion);
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static ProtoBufSerialization getBasicProtoBufSerialization() {
    return new ProtoBufSerialization();
  }

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static FlatBuffersSerialization getStandardFlatBuffersSerialization() {
    return getStandardFlatBuffersSerialization(LionWebVersion.currentVersion);
  }

  public static FlatBuffersSerialization getStandardFlatBuffersSerialization(
      @Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    FlatBuffersSerialization serialization = new FlatBuffersSerialization(lionWebVersion);
    standardInitialization(serialization);
    return serialization;
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static FlatBuffersSerialization getBasicFlatBuffersSerialization() {
    return new FlatBuffersSerialization();
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static FlatBuffersSerialization getBasicFlatBuffersSerialization(
      @Nonnull LionWebVersion lionWebVersion) {
    return new FlatBuffersSerialization(lionWebVersion);
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
