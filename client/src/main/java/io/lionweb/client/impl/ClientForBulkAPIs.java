package io.lionweb.client.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.LionWebVersion;
import io.lionweb.client.RequestFailureException;
import io.lionweb.client.api.BulkAPIClient;
import io.lionweb.client.api.JSONLevelBulkAPIClient;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.model.impl.ProxyNode;
import io.lionweb.utils.CommonChecks;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientForBulkAPIs extends BulkAPIsLionWebClientImplHelper implements BulkAPIClient {

  private final JSONLevelBulkAPIClient jsonLevelClient;

  public ClientForBulkAPIs(ClientConfiguration clientConfiguration) {
    super(clientConfiguration);
    jsonLevelClient = new ClientForJSONLevelBulkAPIs(clientConfiguration);
  }

  @NotNull
  @Override
  public LionWebVersion getLionWebVersion() {
    return conf.getJsonSerialization().getLionWebVersion();
  }

  @Override
  public @Nullable RepositoryVersionToken createPartitions(List<Node> partitions)
      throws IOException {
    return jsonLevelClient.rawCreatePartitions(
        conf.getJsonSerialization()
            .serializeTreesToJsonString(partitions.toArray(new ClassifierInstance[0])));
  }

  @Override
  public @Nullable RepositoryVersionToken deletePartitions(List<String> ids) throws IOException {
    return super.deletePartitions(ids);
  }

  @Override
  public List<String> listPartitionsIDs() throws IOException {
    return listPartitions().stream().map(n -> n.getID()).collect(Collectors.toList());
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
  public @Nullable RepositoryVersionToken store(List<Node> nodes) throws IOException {
    if (nodes.isEmpty()) {
      return null;
    }
    String json =
        conf.getJsonSerialization()
            .serializeTreesToJsonString(nodes.toArray(new ClassifierInstance<?>[0]));
    return jsonLevelClient.rawStore(json);
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
