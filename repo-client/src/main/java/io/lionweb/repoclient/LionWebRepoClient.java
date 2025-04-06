package io.lionweb.repoclient;

import com.google.gson.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import io.lionweb.lioncore.java.serialization.UnavailableNodePolicy;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LionWebRepoClient {

  public class Builder {
    private LionWebVersion lionWebVersion = LionWebVersion.currentVersion;
    private String hostname = "localhost";
    private int port = 3005;
    private String authorizationToken = null;
    private String clientID = "GenericJavaBasedLionWebClient";
    private String repository = "default";
    private long connectTimeoutInSeconds = 60;
    private long callTimeoutInSeconds = 60;

    public Builder withVersion(LionWebVersion version) {
      this.lionWebVersion = version;
      return this;
    }

    public Builder withHostname(String hostname) {
      this.hostname = hostname;
      return this;
    }

    public Builder withPort(int port) {
      this.port = port;
      return this;
    }

    public Builder withAuthorizationToken(String token) {
      this.authorizationToken = token;
      return this;
    }

    public Builder withClientID(String clientID) {
      this.clientID = clientID;
      return this;
    }

    public Builder withRepository(String repository) {
      this.repository = repository;
      return this;
    }

    public Builder withConnectTimeout(long seconds) {
      this.connectTimeoutInSeconds = seconds;
      return this;
    }

    public Builder withCallTimeout(long seconds) {
      this.callTimeoutInSeconds = seconds;
      return this;
    }

    public LionWebRepoClient build() {
      return new LionWebRepoClient(
              lionWebVersion,
              hostname,
              port,
              authorizationToken,
              clientID,
              repository,
              connectTimeoutInSeconds,
              callTimeoutInSeconds
      );
    }
  }

  private static final MediaType JSON = MediaType.get("application/json");
  private static final MediaType PROTOBUF = MediaType.get("application/protobuf");
  private static final MediaType FLATBUFFERS = MediaType.get("application/flatbuffers");

  private final Protocol protocol = Protocol.HTTP;
  private final String hostname;
  private final int port;
  private final String authorizationToken;
  private final String clientID;
  private final String repository;
  private final OkHttpClient httpClient;
  private final Gson gson = new GsonBuilder().serializeNulls().create();
  private final JsonSerialization jsonSerialization;

  //
  // Constructors
  //

  public LionWebRepoClient(@NotNull LionWebVersion lionWebVersion, String hostname, int port, String repository) {
    this(lionWebVersion, hostname, port, null, "GenericJavaBasedLionWebClient", repository, 60, 60);
  }

  public LionWebRepoClient(
          @NotNull LionWebVersion lionWebVersion,
      @NotNull String hostname,
      int port,
      @Nullable String authorizationToken,
      @NotNull String clientID,
      @NotNull String repository,
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

    this.jsonSerialization = SerializationProvider.getStandardJsonSerialization(lionWebVersion);
    this.jsonSerialization.enableDynamicNodes();
    this.jsonSerialization.setUnavailableChildrenPolicy(UnavailableNodePolicy.PROXY_NODES);
    this.jsonSerialization.setUnavailableParentPolicy(UnavailableNodePolicy.PROXY_NODES);
    this.jsonSerialization.setUnavailableReferenceTargetPolicy(UnavailableNodePolicy.PROXY_NODES);
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

  public @NotNull String getRepository() {
    return repository;
  }

  public @NotNull JsonSerialization getJsonSerialization() {
    return jsonSerialization;
  }

  //
  // Bulk APIs
  //

  public void createPartitions(String data) throws IOException {
    nodesStoringOperation(data, "createPartitions");
  }

  public void deletePartitions(List<String> ids) throws IOException {
    JsonArray ja = new JsonArray();
    for (String id : ids) {
      ja.add(id);
    }

    String bodyJson = gson.toJson(ja);
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

  public List<Node> listPartitions() throws IOException {
    String url = protocol.value + "://" + hostname + ":" + port + "/bulk/listPartitions";
    Request.Builder rq =
            new Request.Builder().url(addRepositoryQueryParam(addClientIdQueryParam(url)));
    rq = considerAuthenticationToken(rq);
    Request request =
            rq.addHeader("Accept-Encoding", "gzip").post(RequestBody.create(new byte[0], null)).build();

    try (Response response = httpClient.newCall(request).execute()) {
      String body = Objects.requireNonNull(response.body()).string();
      if (response.code() == HttpURLConnection.HTTP_OK) {
        JsonObject responseData = JsonParser.parseString(body).getAsJsonObject();
        boolean success = responseData.get("success").getAsBoolean();
        if (!success) {
          throw new RequestFailureException(url, response.code(), body);
        }
        return jsonSerialization.deserializeToNodes(responseData.get("chunk"));
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  public List<String> listPartitionsIDs() throws IOException {
    return listPartitions().stream().map(n -> n.getID()).collect(Collectors.toList());
  }

  public List<String> ids(int count) throws IOException {
    if (count < 0) {
      throw new IllegalArgumentException("Count should be greater or equal to zero");
    }
    if (count == 0) {
      return Collections.emptyList();
    }
    String url = protocol.value + "://" + hostname + ":" + port + "/bulk/ids";
    HttpUrl httpUrl = addRepositoryQueryParam(addClientIdQueryParam(url));
    httpUrl = httpUrl.newBuilder().addQueryParameter("count", Integer.toString(count)).build();
    Request.Builder rq =
            new Request.Builder().url(httpUrl);
    rq = considerAuthenticationToken(rq);
    Request request = rq.post(RequestBody.create(new byte[0])).build();

    try (Response response = httpClient.newCall(request).execute()) {
      String body = Objects.requireNonNull(response.body()).string();
      if (response.code() == HttpURLConnection.HTTP_OK) {
        JsonObject responseData = JsonParser.parseString(body).getAsJsonObject();
        boolean success = responseData.get("success").getAsBoolean();
        if (!success) {
          throw new RequestFailureException(url, response.code(), body);
        }
        return responseData.get("ids").getAsJsonArray().asList().stream().map(je -> je.getAsString()).collect(Collectors.toList());
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  public String store(List<String> rootIds, int limit) throws IOException {
    throw new UnsupportedOperationException();
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

  // ──────────────────────────────────────────────────────
  // Helpers
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
          throw new RequestFailureException(url, response.code(), responseBody);
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
