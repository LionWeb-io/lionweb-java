package io.lionweb.client.impl;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.ChunkLevelBulkAPIClient;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.data.SerializedChunk;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
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

  @Nullable
  @Override
  public RepositoryVersionToken createPartitions(@NotNull SerializedChunk data) throws IOException {
    throw new UnsupportedEncodingException();
  }

  @Nullable
  @Override
  public RepositoryVersionToken store(@NotNull SerializedChunk nodes) throws IOException {
    throw new UnsupportedEncodingException();
  }

  @NotNull
  @Override
  public SerializedChunk retrieveAsChunk(@Nullable List<String> nodeIds, int limit)
      throws IOException {
    throw new UnsupportedEncodingException();
  }
}
