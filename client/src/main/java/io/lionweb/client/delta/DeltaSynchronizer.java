package io.lionweb.client.delta;

import io.lionweb.client.delta.messages.commands.ChangeProperty;
import io.lionweb.language.Property;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.ObservableNode;
import io.lionweb.model.ObservableNode.Observer;

import io.lionweb.serialization.data.MetaPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeltaSynchronizer {

    private int nextId = 1;

    private class MyObserver implements Observer {

        private ObservableNode node;

        @Override
        public void propertyChanged(ObservableNode node, @NotNull Property property, @Nullable Object oldValue, @Nullable Object newValue) {
            channel.sendCommand(new ChangeProperty("cmd-"+(nextId++),
                    node.getID(), MetaPointer.from(property), (String)newValue));
        }

        @Override
        public void childAdded(ObservableNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void childRemoved(ObservableNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void referenceValueAdded(ObservableNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void referenceValueChanged(ObservableNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void referenceValueRemoved(ObservableNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void parentChanged(ObservableNode node) {
            throw new UnsupportedOperationException();
        }
    }

    private class MyEventReceiver implements DeltaEventReceiver {

    }

    private DeltaChannel channel;

    public DeltaSynchronizer(DeltaChannel channel) {
        this.channel = channel;
        this.channel.registerEventReceiver(new MyEventReceiver());
    }

    public void attachTree(ObservableNode node) {
        node.registerObserver(new MyObserver());

        ClassifierInstanceUtils.getChildren(node).forEach(c -> attachTree((ObservableNode) c));
    }

}
