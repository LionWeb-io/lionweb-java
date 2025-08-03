package io.lionweb.client.delta;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.commands.ChangeProperty;
import io.lionweb.language.Property;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.model.NodeObserver;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeltaSynchronizer {

  private int nextId = 1;
  private Multimap<String, Node> syncedNodes = ArrayListMultimap.create();

  private class MyObserver implements NodeObserver {

    private Node node;

    @Override
    public void propertyChanged(
        @NotNull Node node, @NotNull Property property, @Nullable Object newValue) {
      channel.sendCommand(
          new ChangeProperty(
              "cmd-" + (nextId++), node.getID(), MetaPointer.from(property), (String) newValue));
    }

    @Override
    public void childAdded(@NotNull Node node) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void childRemoved(@NotNull Node node) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void referenceValueAdded(@NotNull Node node) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void referenceValueChanged(@NotNull Node node) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void referenceValueRemoved(@NotNull Node node) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void parentChanged(@NotNull Node node) {
      throw new UnsupportedOperationException();
    }
  }

  private class MyEventReceiver implements DeltaEventReceiver {
    @Override
    public void receiveEvent(DeltaEvent event) {
      throw new UnsupportedOperationException();
    }
  }

  private DeltaChannel channel;

  public DeltaSynchronizer(DeltaChannel channel) {
    this.channel = channel;
    this.channel.registerEventReceiver(new MyEventReceiver());
  }

  public void attachTree(Node node) {
    node.setObserver(new MyObserver());
    syncedNodes.put(node.getID(), node);

    ClassifierInstanceUtils.getChildren(node).forEach(c -> attachTree(c));
  }
}
