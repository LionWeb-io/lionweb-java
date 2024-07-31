package io.lionweb.serialization.extensions;

import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import java.util.LinkedList;
import java.util.List;

public class BulkImport {

  private final List<AttachPoint> attachPoints;
  private final List<ClassifierInstance<?>> nodes;

  public BulkImport() {
    this(new LinkedList<>(), new LinkedList<>());
  }

  public BulkImport(List<AttachPoint> attachPoints, List<ClassifierInstance<?>> nodes) {
    this.attachPoints = attachPoints;
    this.nodes = nodes;
  }

  public void addNode(ClassifierInstance<?> classifierInstance) {
    nodes.add(classifierInstance);
  }

  public void addAttachPoint(AttachPoint attachPoint) {
    attachPoints.add(attachPoint);
  }

  public List<AttachPoint> getAttachPoints() {
    return attachPoints;
  }

  public List<ClassifierInstance<?>> getNodes() {
    return nodes;
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

    public AttachPoint() {}
  }
}
