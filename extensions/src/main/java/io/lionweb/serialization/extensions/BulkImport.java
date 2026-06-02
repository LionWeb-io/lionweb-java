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
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

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

  public BulkImport(
      @NotNull List<AttachPoint> attachPoints, @NotNull List<ClassifierInstance<?>> nodes) {
    Objects.requireNonNull(attachPoints, "attachPoints cannot be null");
    Objects.requireNonNull(nodes, "nodes cannot be null");
    this.attachPoints = attachPoints;
    if (nodes.isEmpty()) {
      this.nodes = new LinkedList<>();
    } else {
      JsonSerialization jsonSerialization =
          getJsonSerialization(nodes.get(0).getClassifier().getLionWebVersion());
      this.nodes =
          jsonSerialization.serializeNodesToSerializationChunk(nodes).getClassifierInstances();
    }
  }

  public void addNode(@NotNull ClassifierInstance<?> classifierInstance) {
    Objects.requireNonNull(classifierInstance, "classifierInstance cannot be null");
    JsonSerialization jsonSerialization =
        getJsonSerialization(classifierInstance.getClassifier().getLionWebVersion());
    nodes.addAll(
        jsonSerialization
            .serializeNodesToSerializationChunk(classifierInstance)
            .getClassifierInstances());
  }

  public void addAttachPoint(@NotNull AttachPoint attachPoint) {
    Objects.requireNonNull(attachPoint, "attachPoint cannot be null");
    attachPoints.add(attachPoint);
  }

  public @NotNull List<AttachPoint> getAttachPoints() {
    return attachPoints;
  }

  public @NotNull List<SerializedClassifierInstance> getNodes() {
    return nodes;
  }

  public int numberOfNodes() {
    return nodes.size();
  }

  public boolean isEmpty() {
    return nodes.isEmpty();
  }

  public void addNodes(@NotNull List<SerializedClassifierInstance> classifierInstances) {
    Objects.requireNonNull(classifierInstances, "classifierInstances cannot be null");
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

    public AttachPoint(
        @NotNull String container, @NotNull MetaPointer containment, @NotNull String rootId) {
      Objects.requireNonNull(container, "container cannot be null");
      Objects.requireNonNull(containment, "containment cannot be null");
      Objects.requireNonNull(rootId, "rootId cannot be null");
      this.container = container;
      this.containment = containment;
      this.rootId = rootId;
    }

    public AttachPoint(
        @NotNull String container, @NotNull Containment containment, @NotNull String rootId) {
      Objects.requireNonNull(container, "container cannot be null");
      Objects.requireNonNull(containment, "containment cannot be null");
      Objects.requireNonNull(rootId, "rootId cannot be null");
      this.container = container;
      this.containment = MetaPointer.from(containment);
      this.rootId = rootId;
    }

    public AttachPoint() {}
  }
}
