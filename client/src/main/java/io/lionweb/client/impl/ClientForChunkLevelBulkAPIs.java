package io.lionweb.client.impl;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.ChunkLevelBulkAPIClient;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.data.SerializedChunk;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import io.lionweb.serialization.data.SerializedClassifierInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientForChunkLevelBulkAPIs extends LionWebClientImplHelper
    implements ChunkLevelBulkAPIClient {

  public ClientForChunkLevelBulkAPIs(ClientConfiguration clientConfiguration) {
    super(clientConfiguration);
  }

  @NotNull
  @Override
  public LionWebVersion getLionWebVersion() {
    return conf.getJsonSerialization().getLionWebVersion();
  }

  @NotNull
  @Override
  public List<String> listPartitionsIDs() {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public RepositoryVersionToken createPartitionsFromChunk(@NotNull List<SerializedClassifierInstance> data) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public RepositoryVersionToken deletePartitions(List<String> ids) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public RepositoryVersionToken storeChunk(@NotNull List<SerializedClassifierInstance> nodes) throws IOException {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public List<SerializedClassifierInstance> retrieveAsChunk(@Nullable List<String> nodeIds, int limit) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public List<SerializedClassifierInstance> retrieveAsChunk(@Nullable List<String> nodeIds) throws IOException {
    return ChunkLevelBulkAPIClient.super.retrieveAsChunk(nodeIds);
  }
}
