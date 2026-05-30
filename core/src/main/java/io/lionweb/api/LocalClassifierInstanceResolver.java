package io.lionweb.api;

import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This NodeResolver consult a given list of classifier instances to find the one with the requested
 * ID.
 */
public class LocalClassifierInstanceResolver implements ClassifierInstanceResolver {
  private final Map<String, ClassifierInstance<?>> instances = new HashMap<>();

  public LocalClassifierInstanceResolver() {}

  public LocalClassifierInstanceResolver(@Nonnull ClassifierInstance<?>... instances) {
    this(Arrays.asList(instances));
  }

  public LocalClassifierInstanceResolver(@Nonnull List<ClassifierInstance<?>> instances) {
    instances.forEach(this::add);
  }

  /**
   * Adds a classifier instance to the internal collection using its unique identifier.
   *
   * @param instance the classifier instance to be added. Must not be null. The instance's ID,
   *     obtained via {@code getID()}, will act as the key in the internal collection.
   */
  public void add(@Nonnull ClassifierInstance<?> instance) {
    instances.put(instance.getID(), instance);
  }

  @Nullable
  @Override
  public ClassifierInstance<?> resolve(@Nullable String instanceID) {
    if (instanceID == null) {
      return null;
    }
    return instances.get(instanceID);
  }

  /**
   * Adds all the provided classifier instances to the internal collection.
   *
   * @param instances the list of classifier instances to be added. Each instance must not be null,
   *     and the list itself must not be null.
   */
  public void addAll(@Nonnull List<? extends ClassifierInstance<?>> instances) {
    instances.forEach(n -> add(n));
  }

  @Override
  public String toString() {
    return "LocalClassifierInstanceResolver(" + instances.keySet() + ")";
  }

  /**
   * Recursively adds a root node and all its descendants to the internal map of classifier
   * instances.
   *
   * @param root the root node to be added, along with its descendants. Must not be null.
   */
  public void addTree(@Nonnull Node root) {
    add(root);
    ClassifierInstanceUtils.getChildren(root).forEach(c -> addTree(c));
  }
}
