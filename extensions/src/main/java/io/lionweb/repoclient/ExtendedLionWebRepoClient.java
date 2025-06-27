package io.lionweb.repoclient;

import io.lionweb.LionWebVersion;
import io.lionweb.repoclient.impl.ClientForAdditionalAPIs;
import io.lionweb.repoclient.impl.RepoClientConfiguration;
import io.lionweb.serialization.extensions.*;
import io.lionweb.serialization.extensions.AdditionalAPIClient;
import io.lionweb.serialization.extensions.BulkImport;
import io.lionweb.serialization.extensions.Compression;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExtendedLionWebRepoClient extends LionWebRepoClient implements AdditionalAPIClient {

  private final ClientForAdditionalAPIs additionalAPIs;

  public class Builder extends LionWebRepoClient.Builder {
    @Override
    public ExtendedLionWebRepoClient build() {
      return new ExtendedLionWebRepoClient(
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

  public ExtendedLionWebRepoClient(
      LionWebVersion lionWebVersion, String hostname, int port, String repository) {
    super(lionWebVersion, hostname, port, repository);
    RepoClientConfiguration conf = buildRepositoryConfiguration();
    this.additionalAPIs = new ClientForAdditionalAPIs(conf);
  }

  public ExtendedLionWebRepoClient(
      LionWebVersion lionWebVersion,
      String hostname,
      int port,
      String authorizationToken,
      String clientID,
      String repository,
      long connectTimeoutInSeconds,
      long callTimeoutInSeconds) {
    super(
        lionWebVersion,
        hostname,
        port,
        authorizationToken,
        clientID,
        repository,
        connectTimeoutInSeconds,
        callTimeoutInSeconds);
    RepoClientConfiguration conf = buildRepositoryConfiguration();
    this.additionalAPIs = new ClientForAdditionalAPIs(conf);
  }

  @Override
  public void bulkImport(
      @Nonnull BulkImport bulkImport, TransferFormat transferFormat, Compression compression)
      throws IOException {
    additionalAPIs.bulkImport(bulkImport, transferFormat, compression);
  }

  @Override
  public List<NodeInfo> getNodeTree(List<String> nodeIDs, @Nullable Integer depthLimit)
      throws IOException {
    return additionalAPIs.getNodeTree(nodeIDs, depthLimit);
  }
}
