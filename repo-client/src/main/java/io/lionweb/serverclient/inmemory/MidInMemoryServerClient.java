package io.lionweb.serverclient.inmemory;

import io.lionweb.LionWebVersion;
import io.lionweb.serialization.data.SerializedChunk;
import io.lionweb.serverclient.api.DBAdminAPIClient;
import io.lionweb.serverclient.api.RawBulkAPIClient;
import io.lionweb.serverclient.api.RepositoryConfiguration;
import io.lionweb.serverclient.api.RepositoryVersionToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MidInMemoryServerClient implements DBAdminAPIClient {
    private @NotNull InMemoryServer inMemoryServer;
    private @Nullable String repositoryName;

    public MidInMemoryServerClient(@NotNull InMemoryServer inMemoryServer) {
        this.inMemoryServer = inMemoryServer;
    }

    public MidInMemoryServerClient(@NotNull InMemoryServer inMemoryServer, @Nullable String repositoryName) {
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
    public RepositoryVersionToken createPartitions(@NotNull SerializedChunk data) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public RepositoryVersionToken store(@NotNull SerializedChunk nodes) throws IOException {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public SerializedChunk retrieve(@Nullable List<String> nodeIds, int limit) throws IOException {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public SerializedChunk retrieve(@Nullable List<String> nodeIds) throws IOException {
        throw new UnsupportedOperationException();
    }
}
