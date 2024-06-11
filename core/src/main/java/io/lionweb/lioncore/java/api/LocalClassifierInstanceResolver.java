package io.lionweb.lioncore.java.api;

import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This NodeResolver consult a given list of classifier instances to find the one with the requested
 * ID.
 */
public class LocalClassifierInstanceResolver implements ClassifierInstanceResolver {
  private final Map<String, ClassifierInstance<?>> instances = new HashMap<>();

  public LocalClassifierInstanceResolver() {}

  public LocalClassifierInstanceResolver(ClassifierInstance<?>... instances) {
    this(Arrays.asList(instances));
  }

  public LocalClassifierInstanceResolver(List<ClassifierInstance<?>> instances) {
    instances.forEach(n -> add(n));
  }

  public void add(@Nonnull ClassifierInstance<?> instance) {
    instances.put(instance.getID(), instance);
  }

  @Nullable
  @Override
  public ClassifierInstance<?> resolve(String instanceID) {
    return instances.get(instanceID);
  }

  public void addAll(@Nonnull List<? extends ClassifierInstance<?>> instances) {
    instances.forEach(n -> add(n));
  }

  @Override
  public String toString() {
    return "LocalClassifierInstanceResolver(" + instances.keySet() + ")";
  }

  public void addTree(@Nonnull Node root) {
    add(root);
    ClassifierInstanceUtils.getChildren(root).forEach(c -> addTree(c));
  }
}
