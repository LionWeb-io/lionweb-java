package io.lionweb.serialization.extensions;

import io.lionweb.lioncore.java.serialization.SerializationProvider;

public class ExtraSerializationProvider extends SerializationProvider {
  public static ExtraFlatBuffersSerialization getExtraStandardFlatBuffersSerialization() {
    ExtraFlatBuffersSerialization serialization = new ExtraFlatBuffersSerialization();
    standardInitialization(serialization);
    return serialization;
  }

  public static ExtraProtoBufSerialization getExtraStandardProtoBufSerialization() {
    ExtraProtoBufSerialization serialization = new ExtraProtoBufSerialization();
    standardInitialization(serialization);
    return serialization;
  }
}
