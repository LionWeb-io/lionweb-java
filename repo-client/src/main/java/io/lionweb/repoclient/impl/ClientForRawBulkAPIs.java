package io.lionweb.repoclient.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.utils.CommonChecks;
import io.lionweb.repoclient.CompressionSupport;
import io.lionweb.repoclient.RequestFailureException;
import io.lionweb.repoclient.api.RawBulkAPIClient;
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

public class ClientForRawBulkAPIs extends LionWebRepoClientImplHelper implements RawBulkAPIClient {

  public ClientForRawBulkAPIs(RepoClientConfiguration repoClientConfiguration) {
    super(repoClientConfiguration);
  }

  @NotNull
  @Override
  public LionWebVersion getLionWebVersion() {
    return conf.getJsonSerialization().getLionWebVersion();
  }

  @Override
  public @Nullable RepositoryVersionToken rawCreatePartitions(@NotNull String data)
      throws IOException {
    return nodesStoringOperation(data, "createPartitions");
  }

  @Nullable
  @Override
  public RepositoryVersionToken rawStore(@NotNull String json) throws IOException {
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
  public String rawRetrieve(@NotNull List<String> nodeIds, int limit) throws IOException {
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
}
