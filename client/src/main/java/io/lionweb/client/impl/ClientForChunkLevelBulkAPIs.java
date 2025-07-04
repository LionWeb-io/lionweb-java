package io.lionweb.client.impl;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.ChunkLevelBulkAPIClient;
import io.lionweb.client.api.JSONLevelBulkAPIClient;
import io.lionweb.client.api.RepositoryVersionToken;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializedChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientForChunkLevelBulkAPIs extends BulkAPIsLionWebClientImplHelper
    implements ChunkLevelBulkAPIClient {

  private final JSONLevelBulkAPIClient jsonLevelClient;

  public ClientForChunkLevelBulkAPIs(ClientConfiguration clientConfiguration) {
    super(clientConfiguration);
    jsonLevelClient = new ClientForJSONLevelBulkAPIs(clientConfiguration);
  }

  @NotNull
  @Override
  public LionWebVersion getLionWebVersion() {
    return conf.getJsonSerialization().getLionWebVersion();
  }

  @NotNull
  @Override
  public List<String> listPartitionsIDs() throws IOException {
    return super.listPartitionsIDs();
  }

  @Nullable
  @Override
  public RepositoryVersionToken createPartitionsFromChunk(
      @NotNull List<SerializedClassifierInstance> data) throws IOException {
    JsonSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(getLionWebVersion());
    String json =
        serialization.serializeToJsonString(SerializedChunk.fromNodes(getLionWebVersion(), data));
    return jsonLevelClient.rawCreatePartitions(json);
  }

  @Nullable
  @Override
  public RepositoryVersionToken deletePartitions(List<String> ids) throws IOException {
    return super.deletePartitions(ids);
  }

  @Nullable
  @Override
  public RepositoryVersionToken storeChunk(@NotNull List<SerializedClassifierInstance> nodes)
      throws IOException {
    JsonSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(getLionWebVersion());
    String json =
        serialization.serializeToJsonString(SerializedChunk.fromNodes(getLionWebVersion(), nodes));
    return jsonLevelClient.rawStore(json);
  }

  @NotNull
  @Override
  public List<SerializedClassifierInstance> retrieveAsChunk(
      @Nullable List<String> nodeIds, int limit) throws IOException {
    String json = jsonLevelClient.rawRetrieve(nodeIds, limit);
    return new LowLevelJsonSerialization()
        .deserializeSerializationBlock(json)
        .getClassifierInstances();
  }

  @NotNull
  @Override
  public List<SerializedClassifierInstance> retrieveAsChunk(@Nullable List<String> nodeIds)
      throws IOException {
    String json = jsonLevelClient.rawRetrieve(nodeIds);
    return new LowLevelJsonSerialization()
        .deserializeSerializationBlock(json)
        .getClassifierInstances();
  }
}
