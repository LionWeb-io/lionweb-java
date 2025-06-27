package io.lionweb.repoclient.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.LionWebVersion;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.model.impl.ProxyNode;
import io.lionweb.repoclient.RequestFailureException;
import io.lionweb.repoclient.api.BulkAPIClient;
import io.lionweb.repoclient.api.RawBulkAPIClient;
import io.lionweb.repoclient.api.RepositoryVersionToken;
import io.lionweb.utils.CommonChecks;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientForBulkAPIs extends LionWebRepoClientImplHelper implements BulkAPIClient {

  private RawBulkAPIClient rawBulkAPIClient;

  public ClientForBulkAPIs(RepoClientConfiguration repoClientConfiguration) {
    super(repoClientConfiguration);
    rawBulkAPIClient = new ClientForRawBulkAPIs(repoClientConfiguration);
  }

  @NotNull
  @Override
  public LionWebVersion getLionWebVersion() {
    return conf.getJsonSerialization().getLionWebVersion();
  }

  @Override
  public @Nullable RepositoryVersionToken createPartitions(List<Node> partitions)
      throws IOException {
    return rawBulkAPIClient.rawCreatePartitions(
        conf.getJsonSerialization()
            .serializeTreesToJsonString(partitions.toArray(new ClassifierInstance[0])));
  }

  @Override
  public @Nullable RepositoryVersionToken deletePartitions(List<String> ids) throws IOException {
    JsonArray ja = new JsonArray();
    for (String id : ids) {
      ja.add(id);
    }

    String bodyJson = gson.toJson(ja);
    RequestBody body = RequestBody.create(bodyJson, JSON);

    Request.Builder rq = buildRequest("/bulk/deletePartitions");
    Request request = rq.post(body).build();

    return performCall(
        request, (response, responseBody) -> getRepoVersionFromResponse(responseBody));
  }

  @Override
  public List<Node> listPartitions() throws IOException {
    Request.Builder rq = buildRequest("/bulk/listPartitions");
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
  public List<String> ids(int count) throws IOException {
    if (count < 0) {
      throw new IllegalArgumentException("Count should be greater or equal to zero");
    }
    if (count == 0) {
      return Collections.emptyList();
    }
    Map<String, String> params = new HashMap<>();
    params.put("count", Integer.toString(count));
    Request.Builder rq = buildRequest("/bulk/ids", true, true, true, params);
    Request request = rq.post(RequestBody.create(new byte[0])).build();
    return performCall(
        request,
        (response, responseBody) -> {
          JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
          boolean success = responseData.get("success").getAsBoolean();
          if (!success) {
            throw new RequestFailureException(
                request.url().toString(), response.code(), responseBody);
          }
          return responseData.get("ids").getAsJsonArray().asList().stream()
              .map(je -> je.getAsString())
              .collect(Collectors.toList());
        });
  }

  @Override
  public @Nullable RepositoryVersionToken store(List<Node> nodes) throws IOException {
    if (nodes.isEmpty()) {
      return null;
    }
    String json =
        conf.getJsonSerialization()
            .serializeTreesToJsonString(nodes.toArray(new ClassifierInstance<?>[0]));
    return rawBulkAPIClient.rawStore(json);
  }

  @Override
  public List<Node> retrieve(List<String> nodeIds, int limit) throws IOException {
    if (nodeIds.isEmpty()) {
      return Collections.emptyList();
    }
    List<String> invalidIDs =
        nodeIds.stream().filter(id -> !CommonChecks.isValidID(id)).collect(Collectors.toList());
    if (!invalidIDs.isEmpty()) {
      throw new IllegalArgumentException("IDs must all be valid. Invalid IDs found: " + invalidIDs);
    }

    String bodyJson =
        "{\"ids\":["
            + String.join(", ", nodeIds.stream().map(id -> "\"" + id + "\"").toArray(String[]::new))
            + "]}";
    Map<String, String> params = new HashMap<>();
    params.put("depthLimit", String.valueOf(limit));
    Request.Builder rq = buildRequest("/bulk/retrieve", true, true, true, params);
    Request request = rq.post(RequestBody.create(bodyJson, JSON)).build();

    return performCall(
        request,
        (response, responseBody) -> {
          JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
          boolean success = responseData.get("success").getAsBoolean();
          if (!success) {
            throw new RequestFailureException(
                request.url().toString(), response.code(), responseBody);
          }
          List<Node> allNodes =
              conf.getJsonSerialization().deserializeToNodes(responseData.get("chunk"));
          Set<String> idsReturned =
              allNodes.stream()
                  .filter(n -> !(n instanceof ProxyNode))
                  .map(n -> n.getID())
                  .collect(Collectors.toSet());
          // We want to return only the roots of the trees returned. From those, the other nodes can
          // be accessed
          return allNodes.stream()
              .filter(
                  n ->
                      !(n instanceof ProxyNode)
                          && (n.getParent() == null
                              || !idsReturned.contains(n.getParent().getID())))
              .collect(Collectors.toList());
        });
  }
}
