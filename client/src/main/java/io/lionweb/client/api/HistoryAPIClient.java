package io.lionweb.client.api;

import io.lionweb.model.Node;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public interface HistoryAPIClient {
  @NotNull
  List<Node> listPartitions(RepositoryVersionToken repoVersion) throws IOException;

  @NotNull
  List<Node> retrieve(RepositoryVersionToken repoVersion, @NotNull List<String> nodeIds, int limit)
      throws IOException;

  default Node retrieve(RepositoryVersionToken repoVersion, @NotNull String nodeId, int limit)
      throws IOException {
    Objects.requireNonNull(nodeId, "Node ID cannot be null");
    List<Node> res = retrieve(repoVersion, List.of(nodeId), limit);
    return res.stream().filter(n -> Objects.equals(n.getID(), nodeId)).findFirst().get();
  }

  default Node retrieve(RepositoryVersionToken repoVersion, @NotNull String nodeId)
      throws IOException {
    return retrieve(repoVersion, nodeId, Integer.MAX_VALUE);
  }
}
