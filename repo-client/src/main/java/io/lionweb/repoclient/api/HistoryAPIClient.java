package io.lionweb.repoclient.api;

import io.lionweb.lioncore.java.model.Node;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface HistoryAPIClient {
  @NotNull
  List<Node> historyListPartitions(RepositoryVersionToken repoVersion) throws IOException;

  @NotNull
  List<Node> historyRetrieve(
      RepositoryVersionToken repoVersion, @NotNull List<String> nodeIds, int limit)
      throws IOException;

  default Node historyRetrieve(
      RepositoryVersionToken repoVersion, @NotNull String nodeId, int limit) throws IOException {
    List<Node> res = historyRetrieve(repoVersion, Arrays.asList(nodeId), limit);
    Node node = res.stream().filter(n -> n.getID().equals(nodeId)).findFirst().get();
    return node;
  }

  default Node historyRetrieve(RepositoryVersionToken repoVersion, @NotNull String nodeId)
      throws IOException {
    return historyRetrieve(repoVersion, nodeId, Integer.MAX_VALUE);
  }
}
