package io.lionweb.client.delta.messages.queries;

import io.lionweb.client.delta.messages.DeltaQuery;

public class SubscribeToPartitionContents extends DeltaQuery {

    /**
     * TargetNode Node id of the partition this client wants to receive events of.
     */
    private String partition;


}
