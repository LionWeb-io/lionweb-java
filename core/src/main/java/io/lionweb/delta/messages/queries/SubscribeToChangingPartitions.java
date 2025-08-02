package io.lionweb.delta.messages.queries;

import io.lionweb.delta.messages.DeltaQuery;
import io.lionweb.delta.messages.ProtocolMessage;

import java.util.List;

public class SubscribeToChangingPartitions extends DeltaQuery {
    /**
     * Whether this client wants to receive events on newly created partitions (true), or not (false)
     */
    private boolean creation;

    /**
     * Whether this client wants to receive events on deleted partitions (true), or not (false).
     */
    private boolean deletion;

    /**
     * Whether this client wants to automatically subscribe to newly created partitions (true), or not (false).
     */
    private boolean partitions;

}
