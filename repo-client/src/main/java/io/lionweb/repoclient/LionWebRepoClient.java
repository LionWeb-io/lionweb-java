package io.lionweb.repoclient;

import com.google.gson.*;
import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import io.lionweb.lioncore.java.serialization.UnavailableNodePolicy;
import io.lionweb.repoclient.api.*;
import io.lionweb.repoclient.impl.ClientForBulkAPIs;
import io.lionweb.repoclient.impl.ClientForDBAdminAPIs;
import io.lionweb.repoclient.impl.ClientForInspectionAPIs;
import io.lionweb.repoclient.impl.RepoClientConfiguration;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.*;
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

  private final ClientForInspectionAPIs inspectionAPIs;
  private final ClientForDBAdminAPIs dbAdminAPIs;
  private final ClientForBulkAPIs bulkAPIs;

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

    RepoClientConfiguration conf =
        new RepoClientConfiguration(
            protocol,
            hostname,
            port,
            authorizationToken,
            clientID,
            repository,
            httpClient,
            jsonSerialization);
    this.inspectionAPIs = new ClientForInspectionAPIs(conf);
    this.dbAdminAPIs = new ClientForDBAdminAPIs(conf);
    this.bulkAPIs = new ClientForBulkAPIs(conf);
  }

  //
  // Configuration
  //

  public @NotNull JsonSerialization getJsonSerialization() {
    return jsonSerialization;
  }

  //
  // Bulk APIs
  //

  @Override
  public void createPartitions(List<Node> partitions) throws IOException {
    bulkAPIs.createPartitions(partitions);
  }

  public void createPartition(@NotNull Node partition) throws IOException {
    createPartitions(Collections.singletonList(partition));
  }

  public void createPartitions(String data) throws IOException {
    bulkAPIs.createPartitions(data);
  }

  @Override
  public void deletePartitions(List<String> ids) throws IOException {
    bulkAPIs.deletePartitions(ids);
  }

  @Override
  public List<Node> listPartitions() throws IOException {
    return bulkAPIs.listPartitions();
  }

  public List<String> listPartitionsIDs() throws IOException {
    return listPartitions().stream().map(n -> n.getID()).collect(Collectors.toList());
  }

  @Override
  public List<String> ids(int count) throws IOException {
    return bulkAPIs.ids(count);
  }

  @Override
  public void store(List<Node> nodes) throws IOException {
    bulkAPIs.store(nodes);
  }

  public void store(@NotNull Node node) throws IOException {
    store(Collections.singletonList(node));
  }

  public List<Node> retrieve(List<String> nodeIds) throws IOException {
    return retrieve(nodeIds, Integer.MAX_VALUE);
  }

  @Override
  public List<Node> retrieve(List<String> nodeIds, int limit) throws IOException {
    return bulkAPIs.retrieve(nodeIds, limit);
  }

  //
  // DBAdmin APIs
  //

  @Override
  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration)
      throws IOException {
    dbAdminAPIs.createRepository(repositoryConfiguration);
  }

  @Override
  public void deleteRepository(@NotNull String repositoryName) throws IOException {
    dbAdminAPIs.deleteRepository(repositoryName);
  }

  @Override
  public void createDatabase() throws IOException {
    dbAdminAPIs.createDatabase();
  }

  @Override
  public @NotNull Set<RepositoryConfiguration> listRepositories() throws IOException {
    return dbAdminAPIs.listRepositories();
  }

  //
  // Inspection APIs
  //

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier() throws IOException {
    return inspectionAPIs.nodesByClassifier();
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage() throws IOException {
    return inspectionAPIs.nodesByLanguage();
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
}
