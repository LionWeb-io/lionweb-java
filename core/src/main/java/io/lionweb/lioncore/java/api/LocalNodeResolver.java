package io.lionweb.lioncore.java.api;

import io.lionweb.lioncore.java.model.Node;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** This NodeResolver consult a given list of nodes to find the one with the requested ID. */
public class LocalNodeResolver implements NodeResolver {
  private Map<String, Node> nodes = new HashMap<>();

  public LocalNodeResolver() {}

  public LocalNodeResolver(List<Node> nodes) {
    nodes.forEach(n -> add(n));
  }

  public void add(@Nonnull Node node) {
    nodes.put(node.getID(), node);
  }

  @Nullable
  @Override
  public Node resolve(String nodeID) {
    return nodes.get(nodeID);
  }

  public void addAll(@Nonnull List<Node> nodes) {
    nodes.forEach(n -> add(n));
  }

  @Override
  public String toString() {
    return "LocalNodeResolver(" + nodes.keySet() + ")";
  }

  public void addTree(@Nonnull Node root) {
    add(root);
    root.getChildren().forEach(c -> addTree(c));
  }
}
