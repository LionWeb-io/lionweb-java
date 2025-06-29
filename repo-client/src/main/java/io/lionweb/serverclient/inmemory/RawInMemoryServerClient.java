package io.lionweb.serverclient.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.model.Node;
import io.lionweb.serverclient.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RawInMemoryServerClient implements RawBulkAPIClient, DBAdminAPIClient {
    private @NotNull InMemoryServer inMemoryServer;
    private @Nullable String repositoryName;

    public RawInMemoryServerClient(@NotNull InMemoryServer inMemoryServer) {
        this.inMemoryServer = inMemoryServer;
    }

    public RawInMemoryServerClient(@NotNull InMemoryServer inMemoryServer, @Nullable String repositoryName) {
        this.inMemoryServer = inMemoryServer;
        this.repositoryName = repositoryName;
    }

    @Nullable
    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(@Nullable String repositoryName) {
        this.repositoryName = repositoryName;
    }

    @NotNull
    @Override
    public LionWebVersion getLionWebVersion() {
        return inMemoryServer.getRepositoryConfiguration(repositoryName).getLionWebVersion();
    }

    @Override
    public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration)  {
        Objects.requireNonNull(repositoryConfiguration);
        inMemoryServer.createRepository(repositoryConfiguration);
    }

    @Override
    public void deleteRepository(@NotNull String repositoryName) {
        inMemoryServer.deleteRepository(repositoryName);
    }

    @Override
    public void createDatabase()  {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<RepositoryConfiguration> listRepositories(){
        return inMemoryServer.listRepositories();
    }

    @Nullable
    @Override
    public RepositoryVersionToken rawCreatePartitions(@NotNull String data) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public RepositoryVersionToken rawStore(@NotNull String nodes) throws IOException {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String rawRetrieve(@Nullable List<String> nodeIds, int limit) throws IOException {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String rawRetrieve(@Nullable List<String> nodeIds) throws IOException {
        throw new UnsupportedOperationException();
    }
}
