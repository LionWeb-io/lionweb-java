package io.lionweb.lioncore.java.api;

import io.lionweb.lioncore.java.model.ClassifierInstance;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/** This combines several ClassifierInstanceResolvers. */
public class CompositeClassifierInstanceResolver implements ClassifierInstanceResolver {
  private List<ClassifierInstanceResolver> classifierInstanceResolvers = new ArrayList<>();

  public CompositeClassifierInstanceResolver() {}

  public CompositeClassifierInstanceResolver(
      ClassifierInstanceResolver... classifierInstanceResolvers) {
    for (ClassifierInstanceResolver classifierInstanceResolver : classifierInstanceResolvers) {
      add(classifierInstanceResolver);
    }
  }

  public CompositeClassifierInstanceResolver add(
      ClassifierInstanceResolver classifierInstanceResolver) {
    classifierInstanceResolvers.add(classifierInstanceResolver);
    return this;
  }

  @Nullable
  @Override
  public ClassifierInstance<?> resolve(String instanceID) {
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
