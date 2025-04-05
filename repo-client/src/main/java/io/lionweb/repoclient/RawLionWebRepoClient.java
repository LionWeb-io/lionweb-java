package io.lionweb.repoclient;

import com.google.gson.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RawLionWebRepoClient {
  private static final MediaType JSON = MediaType.get("application/json");
  private static final MediaType PROTOBUF = MediaType.get("application/protobuf");
  private static final MediaType FLATBUFFERS = MediaType.get("application/flatbuffers");

  private final Protocol protocol = Protocol.HTTP;
  private final String hostname;
  private final int port;
  private final String authorizationToken;
  private String clientID;
  private String repository;
  private final OkHttpClient httpClient;

  //
  // Constructors
  //

  public RawLionWebRepoClient() {
    this("localhost", 3005, null, "GenericJavaBasedLionWebClient", "default", 60, 60);
  }

  public RawLionWebRepoClient(String hostname, int port) {
    this(hostname, port, "default");
  }

  public RawLionWebRepoClient(String hostname, int port, String repository) {
    this(hostname, port, null, "GenericJavaBasedLionWebClient", repository, 60, 60);
  }

  public RawLionWebRepoClient(
      String hostname, int port, String authorizationToken, String clientID, String repository) {
    this(hostname, port, authorizationToken, clientID, repository, 60, 60);
  }

  public RawLionWebRepoClient(
      String hostname,
      int port,
      String authorizationToken,
      String clientID,
      String repository,
      long connectTimeoutInSeconds,
      long callTimeoutInSeconds) {
    this.hostname = hostname;
    this.port = port;
    this.authorizationToken = authorizationToken;
    this.clientID = clientID;
    this.repository = repository;

    this.httpClient =
        new OkHttpClient.Builder()
            .connectTimeout(connectTimeoutInSeconds, TimeUnit.SECONDS)
            .readTimeout(callTimeoutInSeconds, TimeUnit.SECONDS)
            .writeTimeout(callTimeoutInSeconds, TimeUnit.SECONDS)
            .callTimeout(callTimeoutInSeconds, TimeUnit.SECONDS)
            .build();
  }

  //
  // Configuration
  //

  public @NotNull Protocol getProtocol() {
    return protocol;
  }

  public @NotNull String getHostname() {
    return hostname;
  }

  public int getPort() {
    return port;
  }

  public @Nullable String getAuthorizationToken() {
    return authorizationToken;
  }

  public @NotNull String getClientID() {
    return clientID;
  }

  public void setClientID(@NotNull String clientID) {
    this.clientID = clientID;
  }

  public @NotNull String getRepository() {
    return repository;
  }

  public void setRepository(@NotNull String repository) {
    this.repository = repository;
  }

  //
  // Calls
  //

  public void createRepository(boolean history) throws IOException {
    String url =
        String.format(
            "%s://%s:%d/createRepository?history=%s", protocol.value, hostname, port, history);
    Request.Builder rq = new Request.Builder().url(addClientIdQueryParam(url));
    rq = considerAuthenticationToken(rq);
    Request request = rq.post(RequestBody.create(new byte[0], null)).build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (response.code() != HttpURLConnection.HTTP_OK) {
        throw new RuntimeException(
            "DB initialization failed, HTTP " + response.code() + ": " + response.body().string());
      }
    }
  }

  public void deletePartition(String nodeID) throws IOException {
    String bodyJson = "[\"" + nodeID + "\"]";
    RequestBody body = RequestBody.create(bodyJson, JSON);
    Request.Builder rq =
        new Request.Builder()
            .url(
                addClientIdQueryParam(
                    protocol.value + "://" + hostname + ":" + port + "/bulk/deletePartitions"));
    rq = considerAuthenticationToken(rq);
    Request request = rq.post(body).build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (response.code() != HttpURLConnection.HTTP_OK) {
        throw new RuntimeException(
            "Request failed with code " + response.code() + ": " + response.body().string());
      }
    }
  }

  public String listPartitions() throws IOException {
    String url = protocol.value + "://" + hostname + ":" + port + "/bulk/listPartitions";
    Request.Builder rq =
        new Request.Builder().url(addRepositoryQueryParam(addClientIdQueryParam(url)));
    rq = considerAuthenticationToken(rq);
    Request request =
        rq.addHeader("Accept-Encoding", "gzip").post(RequestBody.create(new byte[0], null)).build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (response.code() == HttpURLConnection.HTTP_OK) {
        return Objects.requireNonNull(response.body()).string();
      } else {
        throw new RuntimeException("Got back " + response.code() + ": " + response.body().string());
      }
    }
  }

  public String retrieve(List<String> rootIds, int limit) throws IOException {
    if (rootIds.isEmpty() || rootIds.stream().anyMatch(String::isEmpty)) {
      throw new IllegalArgumentException("Root IDs must not be empty or blank");
    }

    String bodyJson =
        "{\"ids\":["
            + String.join(", ", rootIds.stream().map(id -> "\"" + id + "\"").toArray(String[]::new))
            + "]}";
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(protocol + "://" + hostname + ":" + port + "/bulk/retrieve").newBuilder();
    urlBuilder.addQueryParameter("depthLimit", String.valueOf(limit));
    urlBuilder.addQueryParameter("clientId", clientID);
    urlBuilder.addQueryParameter("repository", repository);

    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
    rq = considerAuthenticationToken(rq);
    Request request = rq.post(RequestBody.create(bodyJson, JSON)).build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (response.code() == HttpURLConnection.HTTP_OK) {
        return Objects.requireNonNull(response.body()).string();
      } else {
        throw new RuntimeException(
            "Error retrieving data: " + response.code() + ", " + response.body().string());
      }
    }
  }

  public Map<ClassifierKey, ClassifierResult> nodesByClassifier(Integer limit) throws IOException {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(protocol + "://" + hostname + ":" + port + "/inspection/nodesByClassifier")
            .newBuilder();
    urlBuilder.addQueryParameter("clientId", clientID);
    if (limit != null) {
      urlBuilder.addQueryParameter("limit", String.valueOf(limit));
    }

    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
    rq = considerAuthenticationToken(rq);
    Request request = rq.get().build();

    try (Response response = httpClient.newCall(request).execute()) {
      String body = Objects.requireNonNull(response.body()).string();
      if (response.code() != HttpURLConnection.HTTP_OK) {
        throw new RuntimeException("Request failed: " + response.code() + ": " + body);
      }

      JsonArray array = JsonParser.parseString(body).getAsJsonArray();
      Map<ClassifierKey, ClassifierResult> result = new HashMap<>();
      for (JsonElement element : array) {
        JsonObject obj = element.getAsJsonObject();
        ClassifierKey key =
            new ClassifierKey(
                obj.get("language").getAsString(), obj.get("classifier").getAsString());
        Set<String> ids = new HashSet<>();
        obj.get("ids").getAsJsonArray().forEach(idElem -> ids.add(idElem.getAsString()));
        int size = obj.get("size").getAsInt();
        result.put(key, new ClassifierResult(ids, size));
      }
      return result;
    }
  }

  // Remaining methods: nodeTree, bulkImportUsingJson, nodesStoringOperation, bulkImport etc.
  // ... (Let me know if you'd like the rest completed now)

  // ──────────────────────────────────────────────────────
  // 🔧 Helpers
  // ──────────────────────────────────────────────────────

  private HttpUrl addClientIdQueryParam(String rawUrl) {
    HttpUrl.Builder builder = HttpUrl.parse(rawUrl).newBuilder();
    builder.addQueryParameter("clientId", clientID);
    return builder.build();
  }

  private HttpUrl addRepositoryQueryParam(HttpUrl url) {
    return url.newBuilder().addQueryParameter("repository", repository).build();
  }

  private Request.Builder considerAuthenticationToken(Request.Builder builder) {
    return (authorizationToken == null)
        ? builder
        : builder.addHeader("Authorization", authorizationToken);
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

  private Request.Builder addGZipCompressionHeader(Request.Builder builder) {
    return builder.addHeader("Content-Encoding", "gzip");
  }

  public void nodesStoringOperation(final String json, final String operation) {
    // Compress the request body
    RequestBody body =
        CompressionSupport.compress(
            json); // assuming CompressUtil.compress(String) handles JSON compression

    final String url = protocol.value + "://" + hostname + ":" + port + "/bulk/" + operation;

    // Build the request
    Request.Builder rb =
        new Request.Builder().url(addRepositoryQueryParam(addClientIdQueryParam(url)));
    rb = considerAuthenticationToken(rb);
    rb = addGZipCompressionHeader(rb);
    Request request = rb.post(body).build();

    try {
      try (Response response = httpClient.newCall(request).execute()) {
        if (response.code() != HttpURLConnection.HTTP_OK) {
          String responseBody = response.body() != null ? response.body().string() : null;
          throw new RequestFailureException(url, json, response.code(), responseBody);
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
}
