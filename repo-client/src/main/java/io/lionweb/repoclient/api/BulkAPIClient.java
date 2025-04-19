package io.lionweb.repoclient.api;

import io.lionweb.lioncore.java.model.Node;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public interface BulkAPIClient {
  @Nullable Long  createPartitions(List<Node> partitions) throws IOException;

  @Nullable Long  deletePartitions(List<String> ids) throws IOException;

  List<Node> listPartitions() throws IOException;

  List<String> ids(int count) throws IOException;

  @Nullable Long  store(List<Node> nodes) throws IOException;

  List<Node> retrieve(List<String> nodeIds, int limit) throws IOException;
}
