package io.lionweb.repoclient.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.java.utils.CommonChecks;
import io.lionweb.repoclient.CompressionSupport;
import io.lionweb.repoclient.RequestFailureException;
import io.lionweb.repoclient.api.BulkAPIClient;
import io.lionweb.repoclient.api.RepositoryVersionToken;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientForBulkAPIs extends LionWebRepoClientImplHelper implements BulkAPIClient {

  public ClientForBulkAPIs(RepoClientConfiguration repoClientConfiguration) {
    super(repoClientConfiguration);
  }

    @NotNull
    @Override
    public LionWebVersion getLionWebVersion() {
        return conf.getJsonSerialization().getLionWebVersion();
    }

    @Override
  public @Nullable RepositoryVersionToken createPartitions(List<Node> partitions)
      throws IOException {
    return createPartitions(
        conf.getJsonSerialization()
            .serializeTreesToJsonString(partitions.toArray(new ClassifierInstance[0])));
  }

  @Override
  public @Nullable RepositoryVersionToken createPartitions(String data) throws IOException {
    return nodesStoringOperation(data, "createPartitions");
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
    return rawStore(json);
  }

  @Nullable
  @Override
  public RepositoryVersionToken rawStore(String json) throws IOException {
    Request.Builder rq = buildRequest("/bulk/store");
    rq = addGZipCompressionHeader(rq);
    RequestBody uncompressedBody = RequestBody.create(json, JSON);
    Request request = rq.post(gzipCompress(uncompressedBody)).build();
    return performCall(
        request,
        (response, responseBody) -> {
          JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
          boolean success = responseData.get("success").getAsBoolean();
          if (!success) {
            throw new RequestFailureException(
                request.url().toString(), response.code(), responseBody);
          }
          return getRepoVersionFromResponse(responseBody);
        });
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

  @Override
  public String rawRetrieve(List<String> nodeIds, int limit) throws IOException {
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
          JsonElement chunkAsJson = responseData.get("chunk");
          return gson.toJson(chunkAsJson);
        });
  }

  private @Nullable RepositoryVersionToken nodesStoringOperation(
      final String json, final String operation) {
    // Build the request
    Request.Builder rb = buildRequest("/bulk/" + operation);
    rb = addGZipCompressionHeader(rb);
    RequestBody body =
        CompressionSupport.compress(
            json); // assuming CompressUtil.compress(String) handles JSON compression
    Request request = rb.post(body).build();

    String url = request.url().toString();
    try {
      try (Response response = conf.getHttpClient().newCall(request).execute()) {
        String responseBody = response.body() != null ? response.body().string() : null;
        if (response.code() != HttpURLConnection.HTTP_OK) {
          throw new RequestFailureException(url, response.code(), responseBody);
        } else {
          return getRepoVersionFromResponse(responseBody);
        }
      }
    } catch (ConnectException e) {
      String jsonExcerpt = json.length() > 10000 ? json.substring(0, 1000) + "..." : json;
      throw new RuntimeException(
          "Cannot get answer from the client when contacting at URL "
              + url
              + ". Body: "
              + jsonExcerpt,
          e);
    } catch (IOException e) {
      throw new RuntimeException("IO error while contacting URL " + url, e);
    }
  }

  private RequestBody gzipCompress(RequestBody original) throws IOException {
    Buffer buffer = new Buffer();
    original.writeTo(buffer);

    RequestBody gzippedBody =
        new RequestBody() {
          @Override
          public MediaType contentType() {
            return original.contentType();
          }

          @Override
          public long contentLength() {
            return -1; // unknown
          }

          @Override
          public void writeTo(BufferedSink sink) throws IOException {
            GzipSink gzipSink = new GzipSink(sink);
            BufferedSink compressedSink = Okio.buffer(gzipSink);
            buffer.copyTo(compressedSink.buffer(), 0, buffer.size());
            compressedSink.close();
          }
        };

    return gzippedBody;
  }

  private @Nullable RepositoryVersionToken getRepoVersionFromResponse(String responseBody) {
    JsonArray data =
        JsonParser.parseString(responseBody).getAsJsonObject().get("messages").getAsJsonArray();
    Optional<JsonElement> repoVersionMessage =
        data.asList().stream()
            .filter(e -> e.getAsJsonObject().get("kind").getAsString().equals("RepoVersion"))
            .findFirst();
    if (!repoVersionMessage.isPresent()) {
      return null;
    }
    long version =
        repoVersionMessage
            .get()
            .getAsJsonObject()
            .get("data")
            .getAsJsonObject()
            .get("version")
            .getAsLong();
    return new RepositoryVersionToken(Long.toString(version));
  }
}
