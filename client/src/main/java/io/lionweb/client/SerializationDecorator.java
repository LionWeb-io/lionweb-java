package io.lionweb.client;

import io.lionweb.serialization.JsonSerialization;

public interface SerializationDecorator {
  void apply(JsonSerialization serialization);
}
