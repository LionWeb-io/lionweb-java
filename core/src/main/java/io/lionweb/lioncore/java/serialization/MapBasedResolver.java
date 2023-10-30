package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.api.ClassifierInstanceResolver;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * This is used only during deserialization. Some nodes could be an ID that depends on their
 * position, so until we place them they could be a temporarily wrong ID.
 */
class MapBasedResolver implements ClassifierInstanceResolver {
  private final Map<String, ClassifierInstance<?>> instancesByID = new HashMap<>();

  public MapBasedResolver() {}

  public MapBasedResolver(Map<String, ClassifierInstance<?>> instancesByID) {
    this.instancesByID.putAll(instancesByID);
  }

  @Nullable
  @Override
  public ClassifierInstance<?> resolve(String instanceID) {
    return instancesByID.get(instanceID);
  }
}
