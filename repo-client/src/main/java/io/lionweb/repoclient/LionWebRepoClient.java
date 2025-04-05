package io.lionweb.repoclient;

import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.*;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LionWebRepoClient {

  private final String hostname;
  private final int port;
  private final boolean debug;
  private final @Nullable Supplier<JsonSerialization> jsonSerializationProvider;
  private final long connectTimeoutInSeconds;
  private final long callTimeoutInSeconds;
  private final String authorizationToken;
  private final String clientID;
  private final String repository;

  private final List<Language> languages = new ArrayList<Language>();
  private final List<SerializationDecorator> serializationDecorators = new ArrayList<>();

  private final BaseLionWebRepoClient lowLevelRepoClient;
  private JsonSerialization jsonSerialization;

  {
    // init block (runs after all fields are assigned)
    registerSerializationDecorator(
        jsonSerialization -> {
          for (Language language : languages) {
            jsonSerialization.registerLanguage(language);
          }
        });
  }

  public LionWebRepoClient() {
    this("localhost", 3005, "default", null, "GenericJavaBasedLionWebClient");
  }

  public LionWebRepoClient(
      @NotNull String hostname,
      int port,
      @NotNull String repository,
      @Nullable String authorizationToken,
      @NotNull String clientID) {
    this.hostname = hostname;
    this.port = port;
    this.debug = false;
    this.jsonSerializationProvider = null;
    this.connectTimeoutInSeconds = 60;
    this.callTimeoutInSeconds = 60;
    this.authorizationToken = authorizationToken;
    this.clientID = clientID;
    this.repository = repository;

    this.lowLevelRepoClient =
        new BaseLionWebRepoClient(
            hostname,
            port,
            authorizationToken,
            clientID,
            repository,
            connectTimeoutInSeconds,
            callTimeoutInSeconds,
            debug);

    this.defaultJsonSerialization = SerializationProvider.getStandardJsonSerialization();
    this.defaultJsonSerialization.enableDynamicNodes();

    this.jsonSerialization = calculateJsonSerialization();
  }

  /** Exposed for testing purposes */
  public final JsonSerialization defaultJsonSerialization;

  public JsonSerialization getJsonSerialization() {
    return jsonSerialization;
  }

  // Configuration

  public JsonSerialization calculateJsonSerialization() {
    JsonSerialization serialization =
        (jsonSerializationProvider != null)
            ? jsonSerializationProvider.get()
            : defaultJsonSerialization;

    for (SerializationDecorator decorator : serializationDecorators) {
      decorator.apply(serialization);
    }

    return serialization;
  }

  public void updateJsonSerialization() {
    this.jsonSerialization = calculateJsonSerialization();
  }

  public void registerLanguage(Language language) {
    this.languages.add(language);
  }

  public void registerSerializationDecorator(SerializationDecorator decorator) {
    this.serializationDecorators.add(decorator);
  }

  public void cleanSerializationDecorators() {
    this.serializationDecorators.clear();
  }

  // Setup

  public void createRepository() {
    createRepository(false);
  }

  public void createRepository(boolean history) {
    try {
      lowLevelRepoClient.createRepository(history);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create repository", e);
    }
  }

  // Partitions

  public void createPartition(Node node) {
    if (!ClassifierInstanceUtils.getChildren(node).isEmpty()) {
      throw new IllegalArgumentException("When creating a partition, please specify a single node");
    }
    treeStoringOperation(node, "createPartitions");
  }

  public void deletePartition(Node node) {
    if (node.getID() == null) {
      throw new IllegalStateException("Node ID not specified");
    }
    deletePartition(node.getID());
  }

  public void deletePartition(String nodeID) {
    try {
      lowLevelRepoClient.deletePartition(nodeID);
    } catch (IOException e) {
      throw new RuntimeException("Failed to delete partition", e);
    }
  }

  public List<String> getPartitionIDs() {
    throw new UnsupportedOperationException();
    //        try {
    //            String data = lowLevelRepoClient.getPartitionIDs();
    //            return processChunkResponse(data, new ChunkProcessor<List<String>>() {
    //                public List<String> process(JsonElement jsonElement) {
    //                    LowLevelJsonSerialization serialization = new LowLevelJsonSerialization();
    //                    SerializationChunk chunk =
    // serialization.deserializeSerializationBlock(jsonElement);
    //                    List<String> ids = new ArrayList<String>();
    //                    for (Node node : chunk.getClassifierInstances()) {
    //                        if (node.getID() != null) {
    //                            ids.add(node.getID());
    //                        }
    //                    }
    //                    return ids;
    //                }
    //            });
    //        } catch (IOException e) {
    //            throw new RuntimeException("Failed to get partition IDs", e);
    //        }
  }

  private void treeStoringOperation(Node node, String operation) {
    //        verifyNode(node);
    //        String json = jsonSerialization.serializeTreesToJsonString(node);
    //        lowLevelRepoClient.nodesStoringOperation(json, operation);
    throw new UnsupportedOperationException();
  }
}
