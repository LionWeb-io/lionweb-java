package io.lionweb.repoclient.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.repoclient.RequestFailureException;
import io.lionweb.repoclient.api.HistoryAPIClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class ClientForHistoryAPIs extends LionWebRepoClientImplHelper implements HistoryAPIClient {

  public ClientForHistoryAPIs(RepoClientConfiguration repoClientConfiguration) {
    super(repoClientConfiguration);
  }

    @Override
    public @NotNull List<Node> historyListPartitions(long repoVersion) throws IOException {
      Map<String, String> params = new HashMap<>();
      params.put("repoVersion", Long.toString(repoVersion));
      Request.Builder rq = buildRequest("/history/listPartitions", true, true,
              true, params);
      Request request =
              rq.addHeader("Accept-Encoding", "gzip").post(RequestBody.create(new byte[0], null)).build();

      return performCall(
              request,
              (response, responseBody) -> {
                JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
                boolean success = responseData.get("success").getAsBoolean();
                if (!success) {
                  throw new RequestFailureException(
                          request.url().toString(), response.code(), responseBody);
                }
                return conf.getJsonSerialization().deserializeToNodes(responseData.get("chunk"));
              });
    }

    @Override
    public @NotNull List<Node> historyRetrieve(long repoVersion, @NotNull List<String> nodeIds, int limit) throws IOException {
        throw new UnsupportedOperationException();
    }
}
