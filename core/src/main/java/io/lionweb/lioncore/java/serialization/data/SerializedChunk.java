package io.lionweb.lioncore.java.serialization.data;

import java.util.*;
import javax.annotation.Nonnull;

/**
 * This represents a chunk of nodes which have been serialized. The serialization could be
 * inconsistent. This is a low-level representation, intended to represent broken chunks or as an
 * intermediate step during serialization or unserialization.
 */
public class SerializedChunk {

  private Map<String, SerializedNode> nodesByID = new HashMap<>();

  private String serializationFormatVersion;
  private List<MetamodelKeyVersion> metamodels = new ArrayList<>();
  private List<SerializedNode> nodes = new ArrayList<>();

  public void setSerializationFormatVersion(String value) {
    this.serializationFormatVersion = value;
  }

  public String getSerializationFormatVersion() {
    return serializationFormatVersion;
  }

  public List<SerializedNode> getNodes() {
    return nodes;
  }

  public void addNode(SerializedNode node) {
    this.nodesByID.put(node.getID(), node);
    nodes.add(node);
  }

  @Nonnull
  public SerializedNode getNodeByID(String nodeID) {
    SerializedNode node = this.nodesByID.get(nodeID);
    if (node == null) {
      throw new IllegalArgumentException("Cannot find node with ID " + nodeID);
    }
    return node;
  }

  public void addMetamodel(MetamodelKeyVersion metamodel) {
    this.metamodels.add(metamodel);
  }

  public Map<String, SerializedNode> getNodesByID() {
    return nodesByID;
  }

  public List<MetamodelKeyVersion> getMetamodels() {
    return metamodels;
  }

  @Override
  public String toString() {
    return "SerializationBlock{"
        + ", serializationFormatVersion='"
        + serializationFormatVersion
        + '\''
        + ", metamodels="
        + metamodels
        + ", nodes="
        + nodes
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SerializedChunk)) return false;
    SerializedChunk that = (SerializedChunk) o;
    return serializationFormatVersion.equals(that.serializationFormatVersion)
        && metamodels.equals(that.metamodels)
        && nodes.equals(that.nodes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serializationFormatVersion, metamodels, nodes);
  }
}
