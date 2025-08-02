package io.lionweb.delta;

import io.lionweb.language.Property;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.ObservableNode;
import io.lionweb.model.ObservableNode.Observer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeltaSynchronizer {

    private class MyObserver implements Observer {

        private ObservableNode node;

        @Override
        public void propertyChanged(ObservableNode node, @Nonnull Property property, @Nullable Object oldValue, @Nullable Object newValue) {

        }

        @Override
        public void childAdded(ObservableNode node) {

        }

        @Override
        public void childRemoved(ObservableNode node) {

        }

        @Override
        public void referenceValueAdded(ObservableNode node) {

        }

        @Override
        public void referenceValueChanged(ObservableNode node) {

        }

        @Override
        public void referenceValueRemoved(ObservableNode node) {

        }

        @Override
        public void parentChanged(ObservableNode node) {

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
