package org.lionweb.lioncore.java.api;

import org.lionweb.lioncore.java.model.Node;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CascadeNodeResolver implements NodeResolver {
    private List<NodeResolver> nodeResolvers = new ArrayList<>();

    public CascadeNodeResolver() {

    }

    public CascadeNodeResolver(NodeResolver... nodeResolvers) {
        for (NodeResolver nodeResolver: nodeResolvers) {
            add(nodeResolver);
        }
    }

    public CascadeNodeResolver add(NodeResolver nodeResolver) {
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
