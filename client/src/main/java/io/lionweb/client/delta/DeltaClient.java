package io.lionweb.client.delta;

import io.lionweb.LionWebVersion;
import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.commands.properties.ChangeProperty;
import io.lionweb.client.delta.messages.events.properties.PropertyChanged;
import io.lionweb.language.Containment;
import io.lionweb.language.Property;
import io.lionweb.language.Reference;
import io.lionweb.model.*;
import io.lionweb.serialization.PrimitiveValuesSerialization;
import io.lionweb.serialization.data.MetaPointer;
import java.lang.ref.WeakReference;
import java.util.*;
import org.jetbrains.annotations.NotNull;

public class DeltaClient implements DeltaEventReceiver {
  private LionWebVersion lionWebVersion;
  private DeltaChannel channel;
  private MonitoringObserver observer = new MonitoringObserver();
  private HashMap<String, Set<WeakReference<ClassifierInstance<?>>>> nodes = new HashMap<>();
  private PrimitiveValuesSerialization primitiveValuesSerialization =
      new PrimitiveValuesSerialization();

  public DeltaClient(DeltaChannel channel) {
    this(LionWebVersion.currentVersion, channel);
    this.channel = channel;
    this.channel.registerEventReceiver(this);
  }

  public DeltaClient(LionWebVersion lionWebVersion, DeltaChannel channel) {
    this.lionWebVersion = lionWebVersion;
    this.channel = channel;
    this.channel.registerEventReceiver(this);
    this.primitiveValuesSerialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers(
        lionWebVersion);
  }

  /**
   * It is responsibility of the caller to ensure that the partition is initially in sync with the
   * server.
   */
  public void monitor(@NotNull Node partition) {
    Objects.requireNonNull(partition, "partition should not be null");
    synchronized (partition) {
      partition
          .thisAndAllDescendants()
          .forEach(
              n ->
                  nodes
                      .computeIfAbsent(n.getID(), id -> new HashSet<>())
                      .add(new WeakReference<>(n)));
      partition.registerPartitionObserver(observer);
    }
  }

  @Override
  public void receiveEvent(DeltaEvent event) {
    if (event instanceof PropertyChanged) {
      PropertyChanged propertyChanged = (PropertyChanged) event;
      for (WeakReference<ClassifierInstance<?>> classifierInstanceRef :
          nodes.get(propertyChanged.node)) {
        ClassifierInstance<?> classifierInstance = classifierInstanceRef.get();
        if (classifierInstance != null) {
          ClassifierInstanceUtils.setPropertyValueByMetaPointer(
              classifierInstance, propertyChanged.property, propertyChanged.newValue);
        }
      }
    } else {
      throw new UnsupportedOperationException(
          "Unsupported event type: " + event.getClass().getName());
    }
  }

  private class MonitoringObserver implements PartitionObserver {

    @Override
    public void propertyChanged(
        ClassifierInstance<?> classifierInstance,
        Property property,
        Object oldValue,
        Object newValue) {
      channel.sendCommand(
          commandId ->
              new ChangeProperty(
                  commandId,
                  classifierInstance.getID(),
                  MetaPointer.from(property),
                  primitiveValuesSerialization.serialize(property.getType().getID(), newValue)));
    }

    @Override
    public void childAdded(
        ClassifierInstance<?> classifierInstance,
        Containment containment,
        int index,
        Node newChild) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void childRemoved(
        ClassifierInstance<?> classifierInstance,
        Containment containment,
        int index,
        Node removedChild) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void annotationAdded(
        ClassifierInstance<?> node, int index, AnnotationInstance newAnnotation) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void annotationRemoved(
        ClassifierInstance<?> node, int index, AnnotationInstance removedAnnotation) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void referenceValueAdded(
        ClassifierInstance<?> classifierInstance,
        Reference reference,
        ReferenceValue referenceValue) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void referenceValueChanged(
        ClassifierInstance<?> classifierInstance,
        Reference reference,
        int index,
        String oldReferred,
        String oldResolveInfo,
        String newReferred,
        String newResolveInfo) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void referenceValueRemoved(
        ClassifierInstance<?> classifierInstance,
        Reference reference,
        int index,
        ReferenceValue referenceValue) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
}
