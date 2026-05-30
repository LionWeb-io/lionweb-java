package io.lionweb.api;

import io.lionweb.model.ClassifierInstance;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/** This combines several ClassifierInstanceResolvers. */
public class CompositeClassifierInstanceResolver implements ClassifierInstanceResolver {
  private final List<ClassifierInstanceResolver> classifierInstanceResolvers = new ArrayList<>();

  public CompositeClassifierInstanceResolver() {}

  public CompositeClassifierInstanceResolver(
      ClassifierInstanceResolver... classifierInstanceResolvers) {
    for (ClassifierInstanceResolver classifierInstanceResolver : classifierInstanceResolvers) {
      add(classifierInstanceResolver);
    }
  }

  /**
   * Adds a {@link ClassifierInstanceResolver} to this composite resolver.
   *
   * @param classifierInstanceResolver the {@link ClassifierInstanceResolver} to be added
   * @return the current instance of {@code CompositeClassifierInstanceResolver}, allowing for
   *     method chaining
   */
  public CompositeClassifierInstanceResolver add(
      ClassifierInstanceResolver classifierInstanceResolver) {
    classifierInstanceResolvers.add(classifierInstanceResolver);
    return this;
  }

  @Nullable
  @Override
  public ClassifierInstance<?> resolve(@Nullable String instanceID) {
    if (instanceID == null) {
      return null;
    }
    for (ClassifierInstanceResolver classifierInstanceResolver : classifierInstanceResolvers) {
      ClassifierInstance<?> instance = classifierInstanceResolver.resolve(instanceID);
      if (instance != null) {
        return instance;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "CompositeClassifierInstanceResolver(" + classifierInstanceResolvers + ")";
  }
}
