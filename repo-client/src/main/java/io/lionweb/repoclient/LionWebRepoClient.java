package io.lionweb.repoclient;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import io.lionweb.lioncore.java.serialization.UnavailableNodePolicy;
import io.lionweb.repoclient.api.*;
import io.lionweb.repoclient.impl.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LionWebRepoClient
    implements BulkAPIClient, DBAdminAPIClient, InspectionAPIClient, HistoryAPIClient {

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
  protected final JsonSerialization jsonSerialization;

  private final ClientForInspectionAPIs inspectionAPIs;
  private final ClientForDBAdminAPIs dbAdminAPIs;
  private final ClientForBulkAPIs bulkAPIs;
  private final ClientForHistoryAPIs historyAPIs;

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

    RepoClientConfiguration conf = buildRepositoryConfiguration();
    this.inspectionAPIs = new ClientForInspectionAPIs(conf);
    this.dbAdminAPIs = new ClientForDBAdminAPIs(conf);
    this.bulkAPIs = new ClientForBulkAPIs(conf);
    this.historyAPIs = new ClientForHistoryAPIs(conf);
  }

  protected RepoClientConfiguration buildRepositoryConfiguration() {
    return new RepoClientConfiguration(
        protocol,
        hostname,
        port,
        authorizationToken,
        clientID,
        repository,
        httpClient,
        jsonSerialization);
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
  public @Nullable RepositoryVersionToken createPartitions(List<Node> partitions)
      throws IOException {
    return bulkAPIs.createPartitions(partitions);
  }

  public @Nullable RepositoryVersionToken createPartition(@NotNull Node partition)
      throws IOException {
    return createPartitions(Collections.singletonList(partition));
  }

  public @Nullable RepositoryVersionToken createPartitions(String data) throws IOException {
    return bulkAPIs.createPartitions(data);
  }

  @Override
  public @Nullable RepositoryVersionToken deletePartitions(List<String> ids) throws IOException {
    return bulkAPIs.deletePartitions(ids);
  }

  @Override
  public List<Node> listPartitions() throws IOException {
    return bulkAPIs.listPartitions();
  }

  public List<String> listPartitionsIDs() throws IOException {
    return listPartitions().stream().map(Node::getID).collect(Collectors.toList());
  }

  @Override
  public List<String> ids(int count) throws IOException {
    return bulkAPIs.ids(count);
  }

  @Override
  public @Nullable RepositoryVersionToken store(List<Node> nodes) throws IOException {
    return bulkAPIs.store(nodes);
  }

  public @Nullable RepositoryVersionToken store(@NotNull Node node) throws IOException {
    return store(Collections.singletonList(node));
  }

  public List<Node> retrieve(List<String> nodeIds) throws IOException {
    return retrieve(nodeIds, Integer.MAX_VALUE);
  }

  public @NotNull Node retrieve(@NotNull String nodeId) throws IOException {
    Objects.requireNonNull(nodeId, "nodeId should not be null");
    List<Node> nodes = retrieve(Collections.singletonList(nodeId), Integer.MAX_VALUE);
    List<Node> matchingNodes =
        nodes.stream().filter(n -> nodeId.equals(n.getID())).collect(Collectors.toList());
    if (matchingNodes.size() != 1) {
      throw new IllegalArgumentException("Node not found: " + nodeId);
    }
    return matchingNodes.get(0);
  }

  @Override
  public List<Node> retrieve(List<String> nodeIds, int limit) throws IOException {
    return bulkAPIs.retrieve(nodeIds, limit);
  }

  @Nullable
  @Override
  public RepositoryVersionToken rawStore(String nodes) throws IOException {
    return bulkAPIs.rawStore(nodes);
  }

  @Override
  public String rawRetrieve(List<String> nodeIds, int limit) throws IOException {
    return bulkAPIs.rawRetrieve(nodeIds, limit);
  }

  @NotNull
  @Override
  public LionWebVersion getLionWebVersion() {
    return buildRepositoryConfiguration().getJsonSerialization().getLionWebVersion();
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

  //
  // History APIs
  //

  @Override
  public List<Node> listPartitions(RepositoryVersionToken repoVersion) throws IOException {
    return historyAPIs.listPartitions(repoVersion);
  }

  @Override
  public List<Node> retrieve(RepositoryVersionToken repoVersion, List<String> nodeIds, int limit)
      throws IOException {
    return historyAPIs.retrieve(repoVersion, nodeIds, limit);
  }
}
