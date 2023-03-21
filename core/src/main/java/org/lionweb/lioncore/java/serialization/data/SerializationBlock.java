package org.lionweb.lioncore.java.serialization.data;

import javax.annotation.Nonnull;
import java.util.*;

public class SerializationBlock {

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

    public void setNodesByID(Map<String, SerializedNode> nodesByID) {
        this.nodesByID = nodesByID;
    }

    public List<MetamodelKeyVersion> getMetamodels() {
        return metamodels;
    }

    @Override
    public String toString() {
        return "SerializationBlock{" +
                ", serializationFormatVersion='" + serializationFormatVersion + '\'' +
                ", metamodels=" + metamodels +
                ", nodes=" + nodes +
                '}';
    }
}
