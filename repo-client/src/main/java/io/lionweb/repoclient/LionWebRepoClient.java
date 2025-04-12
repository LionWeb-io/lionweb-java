package io.lionweb.repoclient;

import com.google.gson.*;
import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import io.lionweb.lioncore.java.serialization.UnavailableNodePolicy;
import io.lionweb.lioncore.java.utils.CommonChecks;
import io.lionweb.repoclient.api.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LionWebRepoClient implements BulkAPIClient, DBAdminAPIClient, InspectionAPIClient {

  public class Builder {
    protected LionWebVersion lionWebVersion = LionWebVersion.currentVersion;
    protected String hostname = "localhost";
    protected int port = 3005;
    protected String authorizationToken = null;
    protected String clientID = "GenericJavaBasedLionWebClient";
    protected String repository = "default";
    protected long connectTimeoutInSeconds = 60;
    protected long callTimeoutInSeconds = 60;

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
          callTimeoutInSeconds);
    }
  }

  protected static final MediaType JSON = MediaType.get("application/json");

  protected final Protocol protocol = Protocol.HTTP;
  protected final String hostname;
  protected final int port;
  protected final String authorizationToken;
  protected final String clientID;
  protected final String repository;
  protected final OkHttpClient httpClient;
  protected final Gson gson = new GsonBuilder().serializeNulls().create();
  protected final JsonSerialization jsonSerialization;

  //
  // Constructors
  //

  public LionWebRepoClient(
      @NotNull LionWebVersion lionWebVersion, String hostname, int port, String repository) {
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

  @Override
  public void createPartitions(List<Node> partitions) throws IOException {
    createPartitions(
        jsonSerialization.serializeTreesToJsonString(
            partitions.toArray(new ClassifierInstance[0])));
  }

  public void createPartitions(String data) throws IOException {
    nodesStoringOperation(data, "createPartitions");
  }

  @Override
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

  @Override
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

  @Override
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
    Request.Builder rq = new Request.Builder().url(httpUrl);
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
        return responseData.get("ids").getAsJsonArray().asList().stream()
            .map(je -> je.getAsString())
            .collect(Collectors.toList());
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  @Override
  public void store(List<Node> nodes) throws IOException {
    if (nodes.isEmpty()) {
      return;
    }
    String url = protocol.value + "://" + hostname + ":" + port + "/bulk/store";
    HttpUrl httpUrl = addRepositoryQueryParam(addClientIdQueryParam(url));
    Request.Builder rq = new Request.Builder().url(httpUrl);
    rq = addGZipCompressionHeader(rq);
    rq = considerAuthenticationToken(rq);
    String json =
        jsonSerialization.serializeTreesToJsonString(nodes.toArray(new ClassifierInstance<?>[0]));
    RequestBody uncompressedBody = RequestBody.create(json, JSON);
    Request request = rq.post(gzipCompress(uncompressedBody)).build();
    try (Response response = httpClient.newCall(request).execute()) {
      String body = Objects.requireNonNull(response.body()).string();
      if (response.code() == HttpURLConnection.HTTP_OK) {
        JsonObject responseData = JsonParser.parseString(body).getAsJsonObject();
        boolean success = responseData.get("success").getAsBoolean();
        if (!success) {
          throw new RequestFailureException(url, response.code(), body);
        }
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  public List<Node> retrieve(List<String> nodeIds) throws IOException {
    return retrieve(nodeIds, Integer.MAX_VALUE);
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
    String url = protocol + "://" + hostname + ":" + port + "/bulk/retrieve";
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    urlBuilder.addQueryParameter("depthLimit", String.valueOf(limit));
    urlBuilder.addQueryParameter("clientId", clientID);
    urlBuilder.addQueryParameter("repository", repository);

    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
    rq = considerAuthenticationToken(rq);
    Request request = rq.post(RequestBody.create(bodyJson, JSON)).build();

    try (Response response = httpClient.newCall(request).execute()) {
      String body = Objects.requireNonNull(response.body()).string();
      if (response.code() == HttpURLConnection.HTTP_OK) {
        JsonObject responseData = JsonParser.parseString(body).getAsJsonObject();
        boolean success = responseData.get("success").getAsBoolean();
        if (!success) {
          throw new RequestFailureException(url, response.code(), body);
        }
        List<Node> allNodes = jsonSerialization.deserializeToNodes(responseData.get("chunk"));
        Set<String> idsReturned = allNodes.stream().map(n -> n.getID()).collect(Collectors.toSet());
        // We want to return only the roots of the trees returned. From those, the other nodes can
        // be accessed
        return allNodes.stream()
            .filter(n -> !(n instanceof ProxyNode) && (n.getParent() == null || !idsReturned.contains(n.getParent().getID())))
            .collect(Collectors.toList());
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  //
  // DBAdmin APIs
  //

  @Override
  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration)
      throws IOException {
    Objects.requireNonNull(repositoryConfiguration, "repositoryConfiguration should not be null");

    String url = protocol + "://" + hostname + ":" + port + "/createRepository";
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    urlBuilder.addQueryParameter("clientId", clientID);
    urlBuilder.addQueryParameter("repository", repositoryConfiguration.getName());
    urlBuilder.addQueryParameter(
        "lionWebVersion", repositoryConfiguration.getLionWebVersion().getVersionString());
    urlBuilder.addQueryParameter(
        "history", Boolean.toString(repositoryConfiguration.getHistorySupport().toBoolean()));

    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
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
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  @Override
  public void deleteRepository(@NotNull String repositoryName) throws IOException {
    Objects.requireNonNull(repositoryName, "repositoryName should not be null");

    String url = protocol + "://" + hostname + ":" + port + "/deleteRepository";
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    urlBuilder.addQueryParameter("clientId", clientID);
    urlBuilder.addQueryParameter("repository", repositoryName);

    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
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
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  @Override
  public void createDatabase() throws IOException {
    String url = protocol + "://" + hostname + ":" + port + "/createDatabase";
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
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
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  @Override
  public @NotNull Set<RepositoryConfiguration> listRepositories() throws IOException {
    String url = protocol + "://" + hostname + ":" + port + "/listRepositories";
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
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
        return responseData.get("repositories").getAsJsonArray().asList().stream()
            .map(
                el -> {
                  JsonObject elJO = el.getAsJsonObject();
                  String name = elJO.get("name").getAsString();
                  LionWebVersion lionWebVersion =
                      LionWebVersion.fromValue(elJO.get("lionweb_version").getAsString());
                  HistorySupport historySupport =
                      HistorySupport.fromBoolean(elJO.get("history").getAsBoolean());
                  return new RepositoryConfiguration(name, lionWebVersion, historySupport);
                })
            .collect(Collectors.toSet());
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  //
  // Inspection APIs
  //

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier() throws IOException {
    String url = protocol + "://" + hostname + ":" + port + "/inspection/nodesByClassifier";
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    urlBuilder.addQueryParameter("clientId", clientID);
    urlBuilder.addQueryParameter("repository", repository);
    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
    rq = considerAuthenticationToken(rq);
    Request request = rq.get().build();
    try (Response response = httpClient.newCall(request).execute()) {
      String body = Objects.requireNonNull(response.body()).string();
      if (response.code() == HttpURLConnection.HTTP_OK) {
        JsonArray responseData = JsonParser.parseString(body).getAsJsonArray();
        Map<ClassifierKey, ClassifierResult> result = new HashMap<>();
        responseData.forEach(
            entry -> {
              JsonObject entryJO = entry.getAsJsonObject();

              String language = entryJO.get("language").getAsString();
              String classifier = entryJO.get("classifier").getAsString();
              ClassifierKey classifierKey = new ClassifierKey(language, classifier);

              Set<String> ids = new HashSet<>();
              entryJO.get("ids").getAsJsonArray().forEach(el -> ids.add(el.getAsString()));
              int size = entryJO.get("size").getAsInt();
              ClassifierResult classifierResult = new ClassifierResult(ids, size);

              result.put(classifierKey, classifierResult);
            });
        return result;
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage() throws IOException {
    String url = protocol + "://" + hostname + ":" + port + "/inspection/nodesByLanguage";
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    urlBuilder.addQueryParameter("clientId", clientID);
    urlBuilder.addQueryParameter("repository", repository);
    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
    rq = considerAuthenticationToken(rq);
    Request request = rq.get().build();
    try (Response response = httpClient.newCall(request).execute()) {
      String body = Objects.requireNonNull(response.body()).string();
      if (response.code() == HttpURLConnection.HTTP_OK) {
        JsonArray responseData = JsonParser.parseString(body).getAsJsonArray();
        Map<String, ClassifierResult> result = new HashMap<>();
        responseData.forEach(
            entry -> {
              JsonObject entryJO = entry.getAsJsonObject();

              String language = entryJO.get("language").getAsString();

              Set<String> ids = new HashSet<>();
              entryJO.get("ids").getAsJsonArray().forEach(el -> ids.add(el.getAsString()));
              int size = entryJO.get("size").getAsInt();
              ClassifierResult classifierResult = new ClassifierResult(ids, size);

              result.put(language, classifierResult);
            });
        return result;
      } else {
        throw new RequestFailureException(url, response.code(), body);
      }
    }
  }

  // ──────────────────────────────────────────────────────
  // Helpers
  // ──────────────────────────────────────────────────────

  protected HttpUrl addClientIdQueryParam(String rawUrl) {
    HttpUrl.Builder builder = HttpUrl.parse(rawUrl).newBuilder();
    builder.addQueryParameter("clientId", clientID);
    return builder.build();
  }

  protected HttpUrl addRepositoryQueryParam(HttpUrl url) {
    return url.newBuilder().addQueryParameter("repository", repository).build();
  }

  protected Request.Builder considerAuthenticationToken(Request.Builder builder) {
    return (authorizationToken == null)
        ? builder
        : builder.addHeader("Authorization", authorizationToken);
  }

  protected RequestBody gzipCompress(RequestBody original) throws IOException {
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
