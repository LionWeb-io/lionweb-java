package io.lionweb.serialization;

import io.lionweb.LionWebVersion;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.model.impl.ProxyNode;
import io.lionweb.serialization.data.*;
import java.io.*;
import java.util.*;
import javax.annotation.Nonnull;

public class ProtoBufSerialization extends AbstractSerialization {

  public ProtoBufSerialization() {
    super();
  }

  public ProtoBufSerialization(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  /**
   * Deserializes an array of bytes into a list of {@code Node} instances using a default {@code
   * ByteArrayInputStream} for the provided byte array.
   *
   * @param bytes The byte array to be deserialized into {@code Node} instances. It must not be
   *     null.
   * @return A list of {@code Node} instances deserialized from the given byte array.
   * @throws IOException If an I/O error occurs during deserialization.
   */
  public List<Node> deserializeToNodes(byte[] bytes) throws IOException {
    return deserializeToNodes(new ByteArrayInputStream(bytes));
  }

  /**
   * Deserializes the given byte array into a {@code SerializationChunk} instance.
   *
   * @param bytes The byte array representing serialized data. It must not be null.
   * @return The deserialized {@code SerializationChunk} instance.
   * @throws IOException If an I/O error occurs during the deserialization process.
   */
  public SerializationChunk deserializeToChunk(byte[] bytes) throws IOException {
    return DirectProtoBufDeserializer.deserialize(bytes, serializeEmptyFeatures);
  }

  public List<Node> deserializeToNodes(File file) throws IOException {
    return deserializeToNodes(new FileInputStream(file));
  }

  public List<Node> deserializeToNodes(InputStream inputStream) throws IOException {
    SerializationChunk chunk =
        DirectProtoBufDeserializer.deserialize(inputStream, serializeEmptyFeatures);
    validateSerializationBlock(chunk);
    List<ClassifierInstance<?>> all = deserializeSerializationChunk(chunk);
    List<Node> nodes = new ArrayList<>(all.size());
    for (ClassifierInstance<?> ci : all) {
      if (ci instanceof Node) nodes.add((Node) ci);
    }
    return nodes;
  }

  public byte[] serializeTreesToByteArray(ClassifierInstance<?>... roots) {
    Set<String> nodesIDs = new HashSet<>(1024);
    List<ClassifierInstance<?>> allNodes = new ArrayList<>(1024);

    for (ClassifierInstance<?> root : roots) {
      List<ClassifierInstance<?>> classifierInstances = new ArrayList<>();
      ClassifierInstance.collectSelfAndDescendants(root, true, classifierInstances);

      for (ClassifierInstance<?> n : classifierInstances) {
        if (n.getID() != null) {
          if (!nodesIDs.contains(n.getID())) {
            allNodes.add(n);
            nodesIDs.add(n.getID());
          }
        } else {
          allNodes.add(n);
        }
      }
    }

    List<ClassifierInstance<?>> filteredNodes = new ArrayList<>(allNodes.size());
    for (ClassifierInstance<?> node : allNodes) {
      if (!(node instanceof ProxyNode)) {
        filteredNodes.add(node);
      }
    }

    return serializeNodesToByteArray(filteredNodes);
  }

  public byte[] serializeNodesToByteArray(List<ClassifierInstance<?>> classifierInstances) {
    for (ClassifierInstance<?> n : classifierInstances) {
      if (n instanceof ProxyNode) {
        throw new IllegalArgumentException("Proxy nodes cannot be serialized");
      }
    }
    SerializationChunk serializationBlock = serializeNodesToSerializationChunk(classifierInstances);
    return serializeToByteArray(serializationBlock);
  }

  public byte[] serializeNodesToByteArray(ClassifierInstance<?>... classifierInstances) {
    return serializeNodesToByteArray(Arrays.asList(classifierInstances));
  }

  public byte[] serializeToByteArray(SerializationChunk serializationChunk) {
    return DirectProtoBufSerializer.serialize(serializationChunk, serializeEmptyFeatures);
  }
}
