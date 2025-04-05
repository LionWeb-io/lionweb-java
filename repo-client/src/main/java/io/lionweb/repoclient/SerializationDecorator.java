package io.lionweb.repoclient;

import io.lionweb.lioncore.java.serialization.JsonSerialization;

public interface SerializationDecorator {
  void apply(JsonSerialization serialization);
}
