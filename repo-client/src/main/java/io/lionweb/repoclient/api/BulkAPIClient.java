package io.lionweb.repoclient.api;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.Node;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BulkAPIClient {

  @NotNull
  LionWebVersion getLionWebVersion();

  @Nullable
  RepositoryVersionToken createPartitions(List<Node> partitions) throws IOException;

  @Nullable RepositoryVersionToken createPartitions(String data) throws IOException;

  @Nullable
  RepositoryVersionToken deletePartitions(List<String> ids) throws IOException;

  List<Node> listPartitions() throws IOException;

  default List<String> listPartitionsIDs() throws IOException {
    return listPartitions().stream().map(Node::getID).collect(Collectors.toList());
  }

  List<String> ids(int count) throws IOException;

  @Nullable
  RepositoryVersionToken store(List<Node> nodes) throws IOException;

  @Nullable
  RepositoryVersionToken rawStore(String nodes) throws IOException;

  List<Node> retrieve(List<String> nodeIds, int limit) throws IOException;

  String rawRetrieve(List<String> nodeIds, int limit) throws IOException;
}
