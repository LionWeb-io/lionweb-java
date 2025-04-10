package io.lionweb.repoclient.api;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;

public interface DBAdminAPIClient {
    void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration) throws IOException;
    void deleteRepository(@NotNull String repositoryName) throws IOException;
    void createDatabase() throws IOException;
    Set<RepositoryConfiguration> listRepositories() throws IOException;
}
