package io.lionweb.repoclient.api;

import io.lionweb.lioncore.java.model.Node;

import java.io.IOException;
import java.util.List;

public interface BulkAPIClient {
    void createPartitions(List<Node> partitions) throws IOException;
    void deletePartitions(List<String> ids) throws IOException;
    List<Node> listPartitions() throws IOException;
    List<String> ids(int count) throws IOException;
    void store(List<Node> nodes) throws IOException;
    List<Node> retrieve(List<String> nodeIds, int limit) throws IOException;
}
