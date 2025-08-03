package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.commands.ChangeProperty;
import io.lionweb.client.delta.messages.events.PropertyAdded;
import io.lionweb.client.utils.IdentityMultimap;
import io.lionweb.language.Property;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.model.NodeObserver;
import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This object is responsible for ensuring that, the nodes indicated to it ar in sync
 * with some given source.
 *
 * For example, we could sync nodes with a repository, so that changes to the nodes would
 * be reflected on the repository and vice versa.
 */
public abstract class DeltaSynchronizer {

  private int nextId = 1;
  private IdentityMultimap<String, Node> syncedNodes = new IdentityMultimap<>();
  private Map<String, Node> cmdIdsToNode = new IdentityHashMap<>();

  private class MyObserver implements NodeObserver {

    private Node node;

    @Override
    public void propertyChanged(
        @NotNull Node node, @NotNull Property property, @Nullable Object newValue) {
      String cmdId = "cmd-" + (nextId++);
      cmdIdsToNode.put(cmdId, node);
      channel.sendCommand(
          new ChangeProperty(
                  cmdId, node.getID(), MetaPointer.from(property), (String) newValue));
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
      if (event instanceof PropertyAdded) {
        PropertyAdded propertyAdded = (PropertyAdded) event;
        syncedNodes.get(propertyAdded.node).forEach(n ->{
          // Let's exclude the node that caused this
          if (!event.originCommands.stream().allMatch(cmd -> cmdIdsToNode.get(cmd.commandId) == n)) {

            Property property = n.getClassifier().getPropertyByMetaPointer(propertyAdded.property);
            if (Objects.equals(propertyAdded.newValue, n.getPropertyValue(property))) {
              n.setPropertyValue(property, propertyAdded.newValue);
            }
          }
        });

      } else {
        throw new UnsupportedOperationException();
      }
    }
  }

  private DeltaChannel channel;

  public DeltaSynchronizer(DeltaChannel channel) {
    this.channel = channel;
    this.channel.registerEventReceiver(new MyEventReceiver());
  }

  public void attachTree(Node node) {
    forceState(node);

    node.setObserver(new MyObserver());
    syncedNodes.put(node.getID(), node);

    ClassifierInstanceUtils.getChildren(node).forEach(c -> attachTree(c));
  }

  protected abstract void forceState(Node node);
}
