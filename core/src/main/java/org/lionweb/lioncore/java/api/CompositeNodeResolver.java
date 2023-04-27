package org.lionweb.lioncore.java.api;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.lionweb.lioncore.java.model.Node;

/** This combines several NodeResolvers. */
public class CompositeNodeResolver implements NodeResolver {
  private List<NodeResolver> nodeResolvers = new ArrayList<>();

  public CompositeNodeResolver() {}

  public CompositeNodeResolver(NodeResolver... nodeResolvers) {
    for (NodeResolver nodeResolver : nodeResolvers) {
      add(nodeResolver);
    }
  }

  public CompositeNodeResolver add(NodeResolver nodeResolver) {
    nodeResolvers.add(nodeResolver);
    return this;
  }

  @Nullable
  @Override
  public Node resolve(String nodeID) {
    for (NodeResolver nodeResolver : nodeResolvers) {
      Node node = nodeResolver.resolve(nodeID);
      if (node != null) {
        return node;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "CompositeNodeResolver(" + nodeResolvers + ")";
  }
}
