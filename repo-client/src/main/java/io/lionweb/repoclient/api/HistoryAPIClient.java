package io.lionweb.repoclient.api;

import io.lionweb.model.Node;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface HistoryAPIClient {
  @NotNull
  List<Node> listPartitions(RepositoryVersionToken repoVersion) throws IOException;

  @NotNull
  List<Node> retrieve(RepositoryVersionToken repoVersion, @NotNull List<String> nodeIds, int limit)
      throws IOException;

  default Node retrieve(RepositoryVersionToken repoVersion, @NotNull String nodeId, int limit)
      throws IOException {
    List<Node> res = retrieve(repoVersion, Arrays.asList(nodeId), limit);
    Node node = res.stream().filter(n -> n.getID().equals(nodeId)).findFirst().get();
    return node;
  }

  default Node retrieve(RepositoryVersionToken repoVersion, @NotNull String nodeId)
      throws IOException {
    return retrieve(repoVersion, nodeId, Integer.MAX_VALUE);
  }
}
