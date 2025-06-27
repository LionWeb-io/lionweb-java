package io.lionweb.serialization.extensions;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Containment;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BulkImport {

  private static final Map<LionWebVersion, JsonSerialization> jsonSerializations = new HashMap<>();

  private static JsonSerialization getJsonSerialization(LionWebVersion lionWebVersion) {
    return jsonSerializations.computeIfAbsent(
        lionWebVersion, SerializationProvider::getStandardJsonSerialization);
  }

  private final List<AttachPoint> attachPoints;
  private final List<SerializedClassifierInstance> nodes;

  public BulkImport() {
    this(new LinkedList<>(), new LinkedList<>());
  }

  public BulkImport(List<AttachPoint> attachPoints, List<ClassifierInstance<?>> nodes) {
    this.attachPoints = attachPoints;
    if (nodes.isEmpty()) {
      this.nodes = new LinkedList<>();
    } else {
      JsonSerialization jsonSerialization =
          getJsonSerialization(nodes.get(0).getClassifier().getLionWebVersion());
      this.nodes =
          jsonSerialization.serializeNodesToSerializationBlock(nodes).getClassifierInstances();
    }
  }

  public void addNode(ClassifierInstance<?> classifierInstance) {
    JsonSerialization jsonSerialization =
        getJsonSerialization(classifierInstance.getClassifier().getLionWebVersion());
    nodes.addAll(
        jsonSerialization
            .serializeNodesToSerializationBlock(classifierInstance)
            .getClassifierInstances());
  }

  public void addAttachPoint(AttachPoint attachPoint) {
    attachPoints.add(attachPoint);
  }

  public List<AttachPoint> getAttachPoints() {
    return attachPoints;
  }

  public List<SerializedClassifierInstance> getNodes() {
    return nodes;
  }

  public int numberOfNodes() {
    return nodes.size();
  }

  public boolean isEmpty() {
    return nodes.isEmpty();
  }

  public void addNodes(List<SerializedClassifierInstance> classifierInstances) {
    nodes.addAll(classifierInstances);
  }

  public void clear() {
    attachPoints.clear();
    nodes.clear();
  }

  public static class AttachPoint {
    public String container;
    public MetaPointer containment;
    public String rootId;

    public AttachPoint(String container, MetaPointer containment, String rootId) {
      this.container = container;
      this.containment = containment;
      this.rootId = rootId;
    }

    public AttachPoint(String container, Containment containment, String rootId) {
      this.container = container;
      this.containment = MetaPointer.from(containment);
      this.rootId = rootId;
    }

    public AttachPoint() {}
  }
}
