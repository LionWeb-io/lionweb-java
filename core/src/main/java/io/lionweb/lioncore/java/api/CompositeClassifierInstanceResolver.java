package io.lionweb.lioncore.java.api;

import io.lionweb.lioncore.java.model.ClassifierInstance;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/** This combines several NodeResolvers. */
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
  public ClassifierInstance<?> resolve(String nodeID) {
    for (ClassifierInstanceResolver classifierInstanceResolver : classifierInstanceResolvers) {
      ClassifierInstance<?> node = classifierInstanceResolver.resolve(nodeID);
      if (node != null) {
        return node;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "CompositeNodeResolver(" + classifierInstanceResolvers + ")";
  }
}
