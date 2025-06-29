package io.lionweb.client.api;

import io.lionweb.LionWebVersion;
import io.lionweb.model.Node;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BulkAPIClient {

  @NotNull
  LionWebVersion getLionWebVersion();

  @Nullable
  RepositoryVersionToken createPartitions(List<Node> partitions) throws IOException;

  @Nullable
  default RepositoryVersionToken createPartitions(Node... partitions) throws IOException {
    return createPartitions(Arrays.asList(partitions));
  }

  @Nullable
  RepositoryVersionToken deletePartitions(List<String> ids) throws IOException;

  List<Node> listPartitions() throws IOException;

  List<String> listPartitionsIDs() throws IOException;

  List<String> ids(int count) throws IOException;

  @Nullable
  RepositoryVersionToken store(List<Node> nodes) throws IOException;

  @Nullable
  default RepositoryVersionToken store(Node... nodes) throws IOException {
    return store(Arrays.asList(nodes));
  }

  List<Node> retrieve(List<String> nodeIds, int limit) throws IOException;

  default List<Node> retrieve(List<String> nodeIds) throws IOException {
    return retrieve(nodeIds, Integer.MAX_VALUE);
  }
}
