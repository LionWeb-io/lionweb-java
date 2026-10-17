package io.lionweb.client.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.client.BulkRequestFailureException;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.data.SerializationChunk;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class BulkAPIsLionWebClientImplHelper extends LionWebClientImplHelper {

  BulkAPIsLionWebClientImplHelper(ClientConfiguration clientConfiguration) {
    super(clientConfiguration);
  }

  public @NotNull List<String> ids(int count) throws IOException {
    if (count < 0) {
      throw new IllegalArgumentException("Count should be greater or equal to zero");
    }
    if (count == 0) {
      return List.of();
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
            throw new BulkRequestFailureException(
                request.url().toString(), response.code(), responseBody);
          }
          return responseData.get("ids").getAsJsonArray().asList().stream()
              .map(je -> je.getAsString())
              .collect(Collectors.toList());
        });
  }

  List<String> listPartitionsIDs() throws IOException {
    Request.Builder rq = buildRequest("/bulk/listPartitions");
    Request request =
        rq.addHeader("Accept-Encoding", "gzip").post(RequestBody.create(new byte[0], null)).build();

    return performCall(
        request,
        (response, responseBody) -> {
          JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
          boolean success = responseData.get("success").getAsBoolean();
          if (!success) {
            throw new BulkRequestFailureException(
                request.url().toString(), response.code(), responseBody);
          }
          SerializationChunk serializationBlock =
              new LowLevelJsonSerialization()
                  .deserializeSerializationBlock(responseData.get("chunk"));
          return serializationBlock.getClassifierInstances().stream()
              .filter(n -> n.getParentNodeID() == null)
              .map(n -> n.getID())
              .collect(Collectors.toList());
        });
  }

  @Nullable
  RepositoryVersionToken deletePartitions(List<String> ids) throws IOException {
    JsonArray ja = new JsonArray();
    for (String id : ids) {
      ja.add(id);
    }

    String bodyJson = gson.toJson(ja);
    RequestBody body = RequestBody.create(bodyJson, JSON);

    Request.Builder rq = buildRequest("/bulk/deletePartitions");
    Request request = rq.post(body).build();

    return performCall(
        request,
        (response, responseBody) -> {
          checkSuccess(request, response, responseBody);
          return getRepoVersionFromResponse(responseBody);
        });
  }

  /**
   * Query parameters for a retrieve call. The specification has no value for infinite depth: in
   * that case depthLimit must be omitted. Integer.MAX_VALUE is used to express infinite depth.
   */
  static Map<String, String> retrieveParams(int limit) {
    Map<String, String> params = new HashMap<>();
    if (limit != Integer.MAX_VALUE) {
      params.put("depthLimit", String.valueOf(limit));
    }
    return params;
  }

  void checkSuccess(Request request, Response response, @Nullable String responseBody) {
    boolean success =
        responseBody != null
            && JsonParser.parseString(responseBody).getAsJsonObject().get("success").getAsBoolean();
    if (!success) {
      throw new BulkRequestFailureException(
          request.url().toString(), response.code(), responseBody);
    }
  }
}
