package io.lionweb.repoclient.api;

import io.lionweb.lioncore.java.model.Node;

import java.io.IOException;
import java.util.List;

public interface HistoryAPIClient {
    List<Node> historyListPartitions(long repoVersion) throws IOException;
    List<Node> historyRetrieve(long repoVersion, List<String> nodeIds, int limit) throws IOException;
}
