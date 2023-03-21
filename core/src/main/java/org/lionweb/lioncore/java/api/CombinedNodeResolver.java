package org.lionweb.lioncore.java.api;

import org.lionweb.lioncore.java.model.Node;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This combines several NodeResolvers.
 */
public class CombinedNodeResolver implements NodeResolver {
    private List<NodeResolver> nodeResolvers = new ArrayList<>();

    public CombinedNodeResolver() {

    }

    public CombinedNodeResolver(NodeResolver... nodeResolvers) {
        for (NodeResolver nodeResolver: nodeResolvers) {
            add(nodeResolver);
        }
    }

    public CombinedNodeResolver add(NodeResolver nodeResolver) {
        nodeResolvers.add(nodeResolver);
        return this;
    }

    @Nullable
    @Override
    public Node resolve(String nodeID) {
        for (NodeResolver nodeResolver: nodeResolvers) {
            Node node = nodeResolver.resolve(nodeID);
            if (node != null) {
                return node;
            }
        }
        return null;
    }
}
