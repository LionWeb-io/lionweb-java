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

public class InMemoryServerClient implements BulkAPIClient, DBAdminAPIClient {
    private @NotNull InMemoryServer inMemoryServer;
    private @Nullable String repositoryName;

    public InMemoryServerClient(@NotNull InMemoryServer inMemoryServer) {
        this.inMemoryServer = inMemoryServer;
    }

    public InMemoryServerClient(@NotNull InMemoryServer inMemoryServer,  @Nullable String repositoryName) {
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

    @Nullable
    @Override
    public RepositoryVersionToken createPartitions(List<Node> partitions) {
        return inMemoryServer.createPartitions(repositoryName, partitions);
    }

    @Nullable
    @Override
    public RepositoryVersionToken deletePartitions(List<String> ids){
        return inMemoryServer.deletePartitions(repositoryName, ids);
    }

    @Override
    public List<Node> listPartitions() {
        return inMemoryServer.listPartitions(repositoryName);
    }

    @Override
    public List<String> ids(int count){
        return inMemoryServer.ids(repositoryName, count);
    }

    @Nullable
    @Override
    public RepositoryVersionToken store(List<Node> nodes){
        return inMemoryServer.store(repositoryName, nodes);
    }

    @Override
    public List<Node> retrieve(List<String> nodeIds, int limit) {
        throw new UnsupportedOperationException();
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
}
