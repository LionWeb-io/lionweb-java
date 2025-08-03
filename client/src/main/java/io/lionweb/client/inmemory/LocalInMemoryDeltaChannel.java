package io.lionweb.client.inmemory;

import io.lionweb.client.delta.CommandSource;
import io.lionweb.client.delta.DeltaChannel;
import io.lionweb.client.delta.DeltaEventReceiver;
import io.lionweb.client.delta.messages.*;
import io.lionweb.client.delta.messages.commands.ChangeProperty;
import io.lionweb.client.delta.messages.events.PropertyAdded;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LocalInMemoryDeltaChannel implements DeltaChannel {
    private @NotNull InMemoryServer inMemoryServer;
    private String participationId = "DUMMY";
    private int eventSequence = 1;

    public LocalInMemoryDeltaChannel(@NotNull InMemoryServer inMemoryServer, String repositoryName) {
        this.inMemoryServer = inMemoryServer;
        this.eventReceivers.add(event -> {
            if (event instanceof PropertyAdded) {
                PropertyAdded propertyAdded = (PropertyAdded) event;
                inMemoryServer.setProperty(repositoryName, propertyAdded.node, propertyAdded.property, propertyAdded.newValue);
            } else {
                throw new UnsupportedOperationException();
            }
        });
    }

    @Override
    public DeltaQueryResponse sendQuery(DeltaQuery query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeltaCommandResponse sendCommand(DeltaCommand command) {
        if (command instanceof ChangeProperty) {
            ChangeProperty changeProperty = (ChangeProperty) command;
            PropertyAdded event = new PropertyAdded(eventSequence++,
                    changeProperty.node, changeProperty.property, changeProperty.newValue);
            event.addSource(new CommandSource(participationId, command.commandId));
            eventReceivers.forEach(r -> r.receiveEvent(event));
        } else {
            throw new UnsupportedOperationException();
        }

        return new DeltaCommandResponse();
    }

    @Override
    public void registerEventReceiver(DeltaEventReceiver deltaEventReceiver) {
        eventReceivers.add(deltaEventReceiver);
    }

    @Override
    public void unregisterEventReceiver(DeltaEventReceiver deltaEventReceiver) {
        throw new UnsupportedOperationException();
    }

    private List<DeltaEventReceiver> eventReceivers = new ArrayList<>();
}
