package org.lionweb.lioncore.java.api;

import org.lionweb.lioncore.java.model.Node;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalNodeResolver implements NodeResolver {
    private Map<String, Node> nodes = new HashMap<>();

    public LocalNodeResolver() {

    }

    public LocalNodeResolver(List<Node> nodes) {
        nodes.forEach(n -> add(n));
    }

    public void add(Node node) {
        nodes.put(node.getID(), node);
    }

    @Nullable
    @Override
    public Node resolve(String nodeID) {
        return nodes.get(nodeID);
    }

    public void addAll(List<Node> nodes) {
        nodes.forEach(n -> add(n));
    }
}
