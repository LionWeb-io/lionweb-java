package io.lionweb.serialization.extensions;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public final class NodeInfo {
  private final String id;
  private @Nullable final String parent;
  private final int depth;

  public NodeInfo(String id, @Nullable String parent, int depth) {
    this.id = id;
    this.parent = parent;
    this.depth = depth;
  }

  public String getId() {
    return id;
  }

  public @Nullable String getParent() {
    return parent;
  }

  public int getDepth() {
    return depth;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NodeInfo)) return false;
    NodeInfo nodeInfo = (NodeInfo) o;
    return depth == nodeInfo.depth
        && Objects.equals(id, nodeInfo.id)
        && Objects.equals(parent, nodeInfo.parent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, parent, depth);
  }

  @Override
  public String toString() {
    return "NodeInfo{"
        + "id='"
        + id
        + '\''
        + ", parent='"
        + parent
        + '\''
        + ", depth="
        + depth
        + '}';
  }
}
