package io.lionweb.serialization;

import io.lionweb.api.ClassifierInstanceResolver;
import io.lionweb.model.ClassifierInstance;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is used only during deserialization. Some nodes could be an ID that depends on their
 * position, so until we place them they could be a temporarily wrong ID.
 */
class MapBasedResolver implements ClassifierInstanceResolver {
  private final Map<String, ClassifierInstance<?>> instancesByID = new HashMap<>();

  public MapBasedResolver() {}

  public MapBasedResolver(@Nonnull Map<String, ClassifierInstance<?>> instancesByID) {
    Objects.requireNonNull(instancesByID, "instancesByID should not be null");
    this.instancesByID.putAll(instancesByID);
  }

  @Nullable
  @Override
  public ClassifierInstance<?> resolve(@Nullable String instanceID) {
    if (instanceID == null) {
      return null;
    }
    return instancesByID.get(instanceID);
  }
}
