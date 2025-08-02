package io.lionweb.model;

import io.lionweb.language.Property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ObservableNode extends Node {
    void registerObserver(@Nonnull Observer observer);

    interface Observer {
        void propertyChanged(ObservableNode node, @Nonnull Property property, @Nullable Object oldValue, @Nullable Object newValue);
        void childAdded(ObservableNode node);
        void childRemoved(ObservableNode node);
        void referenceValueAdded(ObservableNode node);
        void referenceValueChanged(ObservableNode node);
        void referenceValueRemoved(ObservableNode node);
        void parentChanged(ObservableNode node);
    }
}
