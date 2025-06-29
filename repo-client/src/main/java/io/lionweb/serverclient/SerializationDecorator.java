package io.lionweb.serverclient;

import io.lionweb.serialization.JsonSerialization;

public interface SerializationDecorator {
  void apply(JsonSerialization serialization);
}
