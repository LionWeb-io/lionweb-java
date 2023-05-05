package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.api.NodeResolver;
import io.lionweb.lioncore.java.model.Node;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * This is used only during unserialization. Some nodes could be an ID that depends on their
 * position, so until we place them they could be a temporarily wrong ID.
 */
class MapBasedResolver implements NodeResolver {
  private Map<String, Node> nodesByID = new HashMap<>();

  public MapBasedResolver() {}

  public MapBasedResolver(Map<String, Node> nodesByID) {
    this.nodesByID.putAll(nodesByID);
  }

  @Nullable
  @Override
  public Node resolve(String nodeID) {
    return nodesByID.get(nodeID);
  }
}
