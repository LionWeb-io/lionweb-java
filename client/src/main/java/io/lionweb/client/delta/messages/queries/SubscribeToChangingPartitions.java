package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;

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
