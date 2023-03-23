package org.lionweb.lioncore.java.api;

import org.lionweb.lioncore.java.model.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is able to find a Node given its ID.
 */
public interface NodeResolver {

    /**
     * This returns the Node or null if the Node cannot be found by this NodeResolver.
     */
    @Nullable
    Node resolve(String nodeID);

    @Nonnull
    default Node strictlyResolve(String nodeID) {
        Node partial = resolve(nodeID);
        if (partial == null) {
            throw new UnresolvedNodeException(nodeID);
        } else {
            return partial;
        }
    }
}
