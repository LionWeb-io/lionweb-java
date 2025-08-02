package io.lionweb.delta.messages.queries;

import io.lionweb.delta.messages.DeltaQuery;
import io.lionweb.delta.messages.ProtocolMessage;

import java.util.List;

public class SubscribeToPartitionContents extends DeltaQuery {

    /**
     * TargetNode Node id of the partition this client wants to receive events of.
     */
    private String partition;


}
