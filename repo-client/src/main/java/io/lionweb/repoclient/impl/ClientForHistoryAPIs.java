package io.lionweb.repoclient.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.utils.CommonChecks;
import io.lionweb.repoclient.CompressionSupport;
import io.lionweb.repoclient.RequestFailureException;
import io.lionweb.repoclient.api.BulkAPIClient;
import io.lionweb.repoclient.api.HistoryAPIClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

public class ClientForHistoryAPIs extends LionWebRepoClientImplHelper implements HistoryAPIClient {

  public ClientForHistoryAPIs(RepoClientConfiguration repoClientConfiguration) {
    super(repoClientConfiguration);
  }

    @Override
    public List<Node> historyListPartitions(long repoVersion) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Node> historyRetrieve(long repoVersion, List<String> nodeIds, int limit) throws IOException {
        throw new UnsupportedOperationException();
    }
}
