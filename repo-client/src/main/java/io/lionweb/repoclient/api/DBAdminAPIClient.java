package io.lionweb.repoclient.api;

import java.io.IOException;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface DBAdminAPIClient {
  void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration)
      throws IOException;

  void deleteRepository(@NotNull String repositoryName) throws IOException;

  /**
   * Trigger the creation of the database, according to the configuration provided on the server
   * side. In other words, we cannot dictate the name of the configuration of the database, just
   * triggers its creation.
   */
  void createDatabase() throws IOException;

  @NotNull
  Set<RepositoryConfiguration> listRepositories() throws IOException;
}
