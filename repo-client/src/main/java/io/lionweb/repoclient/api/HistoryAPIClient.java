package io.lionweb.repoclient.api;

import io.lionweb.lioncore.java.model.Node;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public interface HistoryAPIClient {
    @NotNull List<Node> historyListPartitions(long repoVersion) throws IOException;
    @NotNull List<Node> historyRetrieve(long repoVersion, @NotNull List<String> nodeIds, int limit) throws IOException;
}
