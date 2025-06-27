package io.lionweb.repoclient;

import io.lionweb.serialization.JsonSerialization;

public interface SerializationDecorator {
  void apply(JsonSerialization serialization);
}
